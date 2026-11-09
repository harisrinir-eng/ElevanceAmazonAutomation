package elevanceskills.intern;

import java.net.URI;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Shared helpers for all page objects. */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final int manualWaitSeconds;

    protected BasePage(WebDriver driver, WebDriverWait wait, int manualWaitSeconds) {
        this.driver = driver;
        this.wait = wait;
        this.manualWaitSeconds = manualWaitSeconds;
    }

    protected boolean exists(By by) {
        return !driver.findElements(by).isEmpty();
    }

    /** Visible text, falling back to textContent (needed for hidden "a-offscreen" price spans). */
    protected String text(WebElement element) {
        try {
            String t = element.getText();
            if (t == null || t.isBlank()) {
                t = element.getDomProperty("textContent");
            }
            return t == null ? "" : t.replaceAll("\\s+", " ").trim();
        } catch (StaleElementReferenceException e) {
            return "";
        }
    }

    protected String firstText(By by) {
        for (WebElement e : driver.findElements(by)) {
            String t = text(e);
            if (!t.isEmpty()) {
                return t;
            }
        }
        return "";
    }

    protected void click(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", element);
        try {
            element.click();
        } catch (ElementClickInterceptedException | StaleElementReferenceException e) {
            js.executeScript("arguments[0].click();", element);
        }
    }

    protected String origin() {
        URI uri = URI.create(driver.getCurrentUrl());
        return uri.getScheme() + "://" + uri.getHost();
    }

    public boolean isCaptchaPage() {
        String title = driver.getTitle();
        String url = driver.getCurrentUrl();
        return (title != null && title.toLowerCase().contains("robot check"))
                || (url != null && url.contains("validateCaptcha"))
                || exists(By.id("captchacharacters"))
                || exists(By.cssSelector("form[action*='validateCaptcha']"));
    }

    /**
     * If Amazon shows a CAPTCHA, give a human the configured time to solve it manually.
     * Nothing is bypassed: if it stays unsolved the step is reported as BLOCKED.
     */
    public void handleCaptcha() {
        if (!isCaptchaPage()) {
            return;
        }
        if (manualWaitSeconds <= 0) {
            throw new BlockedException("Amazon showed a CAPTCHA / robot-check page. It is not bypassed; "
                    + "set browser.manualInterventionSeconds > 0 and solve it manually, or retry later.");
        }
        System.out.println("CAPTCHA detected - please solve it in the browser window within "
                + manualWaitSeconds + " seconds.");
        try {
            new WebDriverWait(driver, Duration.ofSeconds(manualWaitSeconds))
                    .pollingEvery(Duration.ofSeconds(2))
                    .until(d -> !isCaptchaPage());
        } catch (TimeoutException e) {
            throw new BlockedException("CAPTCHA was not solved within " + manualWaitSeconds + " seconds.");
        }
    }
}
