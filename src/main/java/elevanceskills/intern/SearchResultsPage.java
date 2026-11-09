package elevanceskills.intern;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import elevanceskills.intern.utils.PriceUtil;

/** Search results list and the left-hand filter controls. */
public class SearchResultsPage extends BasePage {

    /** Snapshot of one result card. price/rating are null when the card does not show them. */
    public record ResultItem(String asin, String title, BigDecimal price, Double rating, String url) {
    }

    private static final By CARD = By.cssSelector("div[data-component-type='s-search-result'][data-asin]");

    public SearchResultsPage(WebDriver driver, WebDriverWait wait, int manualWaitSeconds) {
        super(driver, wait, manualWaitSeconds);
    }

    /** @return false when no result cards appear (no results, or layout changed) */
    public boolean waitForResults() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(CARD));
            return true;
        } catch (TimeoutException e) {
            handleCaptcha();
            return false;
        }
    }

    /** Reads up to {@code max} result cards into plain objects (safe to use after navigating away). */
    public List<ResultItem> getResults(int max) {
        List<ResultItem> items = new ArrayList<>();
        if (!waitForResults()) {
            return items;
        }
        String origin = origin();
        for (WebElement card : driver.findElements(CARD)) {
            if (items.size() >= max) {
                break;
            }
            String asin = card.getDomAttribute("data-asin");
            if (asin == null || asin.isBlank()) {
                continue;
            }
            String title = "";
            List<WebElement> titles = card.findElements(By.cssSelector("h2 span, h2"));
            if (!titles.isEmpty()) {
                title = text(titles.get(0));
            }
            BigDecimal price = null;
            List<WebElement> prices = card.findElements(By.cssSelector("span.a-price:not(.a-text-price) span.a-offscreen"));
            if (!prices.isEmpty()) {
                price = PriceUtil.parsePrice(text(prices.get(0))).orElse(null);
            }
            Double rating = null;
            List<WebElement> ratings = card.findElements(
                    By.cssSelector("span.a-icon-alt, [aria-label*='out of 5 stars']"));
            if (!ratings.isEmpty()) {
                String raw = text(ratings.get(0));
                if (raw.isEmpty()) {
                    raw = String.valueOf(ratings.get(0).getDomAttribute("aria-label"));
                }
                rating = PriceUtil.parseRating(raw).orElse(null);
            }
            // Always use the canonical /dp/ASIN URL instead of sponsored redirect links.
            items.add(new ResultItem(asin, title, price, rating, origin + "/dp/" + asin));
        }
        return items;
    }

    // ------------------------------------------------------------------ filters

    /** Clicks the first visible brand filter whose name starts with {@code initial}. */
    public Optional<String> applyBrandFilterStartingWith(char initial) {
        for (WebElement link : driver.findElements(By.cssSelector("#brandsRefinements li a, div[id^='brandsRefinements'] li a"))) {
            String name = text(link);
            if (!name.isEmpty() && Character.toUpperCase(name.charAt(0)) == Character.toUpperCase(initial)) {
                String before = driver.getCurrentUrl();
                click(link);
                waitAfterFilter(before);
                return Optional.of(name);
            }
        }
        return Optional.empty();
    }

    /** Types a minimum price into the visible price box and submits it. */
    public boolean applyMinPriceFilter(int minPrice) {
        List<WebElement> boxes = driver.findElements(By.id("low-price"));
        if (boxes.isEmpty() || !boxes.get(0).isDisplayed()) {
            return false;
        }
        String before = driver.getCurrentUrl();
        WebElement box = boxes.get(0);
        box.clear();
        box.sendKeys(String.valueOf(minPrice));
        List<WebElement> go = driver.findElements(By.xpath(
                "//input[@id='low-price']/ancestor::form//input[@type='submit' or contains(@class,'a-button-input')]"));
        if (!go.isEmpty()) {
            click(go.get(0));
        } else {
            box.sendKeys(Keys.ENTER);
        }
        waitAfterFilter(before);
        return true;
    }

    /** Clicks the "N Stars &amp; Up" customer-review filter. */
    public boolean applyRatingFilter(int stars) {
        String starsText = stars + " stars";
        String outOfText = stars + " out of 5";
        for (WebElement link : driver.findElements(By.cssSelector("#reviewsRefinements li a"))) {
            String all = (text(link) + " " + link.getDomAttribute("aria-label")).toLowerCase();
            if (all.contains(starsText) || all.contains(outOfText)) {
                String before = driver.getCurrentUrl();
                click(link);
                waitAfterFilter(before);
                return true;
            }
        }
        return false;
    }

    private void waitAfterFilter(String urlBefore) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> !d.getCurrentUrl().equals(urlBefore));
        } catch (TimeoutException ignored) {
            // some filters refresh results without changing the URL
        }
        waitForResults();
    }
}
