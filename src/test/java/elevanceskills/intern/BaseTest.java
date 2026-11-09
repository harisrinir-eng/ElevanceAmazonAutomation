
package elevanceskills.intern;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;

import elevanceskills.intern.utils.BrowserFactory;
import elevanceskills.intern.utils.ConfigUtil;
import elevanceskills.intern.utils.ScreenshotUtil;
import elevanceskills.intern.utils.TimeValidator;

public abstract class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected int manualSeconds;

    protected final TimeValidator timeValidator =
        new TimeValidator();

    protected void log(String message) {
        Reporter.log(message, true);
    }

    protected String baseUrl() {
        return ConfigUtil.require("amazon.baseUrl");
    }

    protected void runTask(
            TimeValidator.Window window,
            Runnable body) {

        if (window != null) {

            String info = timeValidator.describe(window);

            if (!timeValidator.isAllowed(window)) {
                log("SKIPPED - not executed. " + info);
                throw new SkipException(
                    "Not executed: " + info
                );
            }

            log(info);
        }

        startBrowser();

        try {
            body.run();

        } catch (BlockedException e) {

            ScreenshotUtil.capture(driver, "blocked");

            log("BLOCKED: " + e.getMessage());

            throw new SkipException(
                "BLOCKED (not passed): " + e.getMessage(), e
            );
        }
    }

    private void startBrowser() {

        boolean headless =
            ConfigUtil.getBoolean("browser.headless");

        manualSeconds = headless
            ? 0
            : ConfigUtil.getInt(
                "browser.manualInterventionSeconds"
            );

        // Initialize Google Chrome
        driver = BrowserFactory.createChrome(headless);

        wait = new WebDriverWait(
            driver,
            Duration.ofSeconds(
                ConfigUtil.getInt("explicitWait.seconds")
            )
        );
    }

    protected AmazonHomePage homePage() {
        return new AmazonHomePage(
            driver, wait, manualSeconds
        );
    }

    protected ProductPage productPage() {
        return new ProductPage(
            driver, wait, manualSeconds
        );
    }

    protected CartPage cartPage() {
        return new CartPage(
            driver, wait, manualSeconds
        );
    }

    protected LoginPage loginPage() {
        return new LoginPage(
            driver, wait, manualSeconds
        );
    }

    protected AccountPage accountPage() {
        return new AccountPage(
            driver, wait, manualSeconds
        );
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {

        if (driver == null) {
            return;
        }

        if (result.getStatus() == ITestResult.FAILURE) {

            var shot = ScreenshotUtil.capture(
                driver,
                result.getMethod().getMethodName()
            );

            log("Failure screenshot: " + shot);
        }

        try {
            driver.quit();
        } finally {
            driver = null;
        }
    }
}
