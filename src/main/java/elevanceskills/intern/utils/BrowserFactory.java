
package elevanceskills.intern.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Creates Google Chrome sessions.
 *
 * Selenium Manager automatically resolves/downloads ChromeDriver.
 * If automatic download fails, configure CHROMEDRIVER_PATH.
 */
public final class BrowserFactory {

    private BrowserFactory() {
    }

    public static WebDriver createChrome(boolean headless) {

        configureDriverPath();

        ChromeOptions options = new ChromeOptions();

        options.addArguments(
            "--disable-notifications",
            "--lang=en-IN"
        );

        if (headless) {
            options.addArguments(
                "--headless=new",
                "--window-size=1920,1080"
            );
        } else {
            options.addArguments("--start-maximized");
        }

        return new ChromeDriver(options);
    }

    private static void configureDriverPath() {

        String driverPath =
            System.getProperty("webdriver.chrome.driver");

        if (driverPath == null || driverPath.isBlank()) {
            driverPath = System.getenv("CHROMEDRIVER_PATH");
        }

        if (driverPath != null && !driverPath.isBlank()) {
            System.setProperty(
                "webdriver.chrome.driver",
                driverPath.trim()
            );
        }
    }
}
