package elevanceskills.intern;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Amazon.in home page and the header search box (which exists on every page). */
public class AmazonHomePage extends BasePage {

    private static final By SEARCH_BOX = By.id("twotabsearchtextbox");

    public AmazonHomePage(WebDriver driver, WebDriverWait wait, int manualWaitSeconds) {
        super(driver, wait, manualWaitSeconds);
    }

    public AmazonHomePage open(String baseUrl) {
        driver.get(baseUrl);
        handleCaptcha();
        dismissInterstitials();
        wait.until(ExpectedConditions.visibilityOfElementLocated(SEARCH_BOX));
        return this;
    }

    /** Cookie banner and the "Continue shopping" interstitial, when present. */
    private void dismissInterstitials() {
        By[] candidates = {
                By.id("sp-cc-accept"),
                By.xpath("//button[normalize-space()='Continue shopping']"),
                By.cssSelector("input[name='glowDoThisLater']")
        };
        for (By by : candidates) {
            for (WebElement e : driver.findElements(by)) {
                if (e.isDisplayed()) {
                    click(e);
                    break;
                }
            }
        }
    }

    public SearchResultsPage search(String keyword) {
        WebElement box = wait.until(ExpectedConditions.visibilityOfElementLocated(SEARCH_BOX));
        box.clear();
        box.sendKeys(keyword, Keys.ENTER);
        handleCaptcha();
        return new SearchResultsPage(driver, wait, manualWaitSeconds);
    }
}
