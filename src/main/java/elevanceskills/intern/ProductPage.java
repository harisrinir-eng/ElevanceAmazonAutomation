package elevanceskills.intern;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import elevanceskills.intern.utils.PriceUtil;
import elevanceskills.intern.utils.ValidationUtil;

/** Product detail page: title, price, brand, rating, breadcrumb and Add to Cart. */
public class ProductPage extends BasePage {

    private static final By TITLE = By.id("productTitle");

    private static final List<By> PRICE_LOCATORS = List.of(
            By.cssSelector("#corePrice_feature_div span.a-price:not(.a-text-price) span.a-offscreen"),
            By.cssSelector("#corePriceDisplay_desktop_feature_div span.a-price:not(.a-text-price) span.a-offscreen"),
            By.cssSelector("#apex_desktop span.a-price:not(.a-text-price) span.a-offscreen"),
            By.cssSelector("#priceblock_ourprice"),
            By.cssSelector("#priceblock_dealprice"),
            By.cssSelector("#price_inside_buybox"),
            By.cssSelector("#centerCol span.a-price:not(.a-text-price) span.a-offscreen"));

    private static final By ADDED_CONFIRMATION = By.cssSelector(
            "#sw-atc-details-single-container, #attach-added-to-cart-message, #huc-v2-order-row-confirm-text, "
                    + "#NATC_SMART_WISHLIST_ACCEPT, #attachDisplayAddBaseAlert, #sw-atc-confirmation");

    public ProductPage(WebDriver driver, WebDriverWait wait, int manualWaitSeconds) {
        super(driver, wait, manualWaitSeconds);
    }

    /** Opens the URL; returns false if no product title appears (404, unavailable, changed layout). */
    public boolean open(String url) {
        driver.get(url);
        handleCaptcha();
        return waitLoaded();
    }

    public boolean waitLoaded() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(TITLE));
            return true;
        } catch (TimeoutException e) {
            handleCaptcha();
            return false;
        }
    }

    public String title() {
        return firstText(TITLE);
    }

    /** Displayed price, or empty when none is shown (e.g. "Currently unavailable"). */
    public Optional<BigDecimal> price() {
        for (By locator : PRICE_LOCATORS) {
            for (WebElement e : driver.findElements(locator)) {
                Optional<BigDecimal> parsed = PriceUtil.parsePrice(text(e));
                if (parsed.isPresent()) {
                    return parsed;
                }
            }
        }
        return Optional.empty();
    }

    public Optional<String> brand() {
        String raw = firstText(By.id("bylineInfo"));
        if (raw.isEmpty()) {
            raw = firstText(By.cssSelector("tr.po-brand td.a-span9 span"));
        }
        String brand = ValidationUtil.cleanBrand(raw);
        return brand.isEmpty() ? Optional.empty() : Optional.of(brand);
    }

    public Optional<Double> rating() {
        List<WebElement> popover = driver.findElements(By.id("acrPopover"));
        if (!popover.isEmpty()) {
            Optional<Double> r = PriceUtil.parseRating(popover.get(0).getDomAttribute("title"));
            if (r.isPresent()) {
                return r;
            }
        }
        return PriceUtil.parseRating(firstText(By.cssSelector("#averageCustomerReviews span.a-icon-alt, #acrPopover span.a-icon-alt")));
    }

    public String breadcrumb() {
        return firstText(By.id("wayfinding-breadcrumbs_feature_div"));
    }

    /** Clicks Add to Cart. Returns false if the button is missing (variant needed, unavailable) or no confirmation appears. */
    public boolean addToCart() {
        List<WebElement> buttons = driver.findElements(By.id("add-to-cart-button"));
        if (buttons.isEmpty()) {
            return false;
        }
        int before = cartCount();
        click(buttons.get(0));
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> exists(ADDED_CONFIRMATION) || cartCount() > before);
        } catch (TimeoutException e) {
            return false;
        }
        for (By decline : new By[] {By.id("attachSiblingDoNotAddCoverage"), By.id("siNoCoverage-announce")}) {
            for (WebElement e : driver.findElements(decline)) {
                if (e.isDisplayed()) {
                    click(e);
                }
            }
        }
        return true;
    }

    private int cartCount() {
        String t = firstText(By.id("nav-cart-count")).replaceAll("[^0-9]", "");
        return t.isEmpty() ? 0 : Integer.parseInt(t);
    }
}
