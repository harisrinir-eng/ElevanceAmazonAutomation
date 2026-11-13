
package elevanceskills.intern;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Amazon sign-in page.
 * Supports environment credentials and manual CAPTCHA/OTP login.
 */
public class LoginPage extends BasePage {

    private static final By ACCOUNT_LINK =
            By.id("nav-link-accountList");

    private static final By HEADER_GREETING =
            By.id("nav-link-accountList-nav-line-1");

    public LoginPage(
            WebDriver driver,
            WebDriverWait wait,
            int manualWaitSeconds) {

        super(driver, wait, manualWaitSeconds);
    }

    public LoginPage openSignIn() {

        click(wait.until(
                ExpectedConditions.elementToBeClickable(ACCOUNT_LINK)
        ));

        handleCaptcha();

        return this;
    }

    public boolean emailFieldVisible() {

        return exists(By.id("ap_email"))
                && driver.findElement(By.id("ap_email")).isDisplayed();
    }

    public void enterEmail(String email) {

        WebElement field = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("ap_email")
                )
        );

        field.clear();
        field.sendKeys(email, Keys.ENTER);
    }

    public void enterPassword(String password) {

        WebElement field = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("ap_password")
                )
        );

        field.clear();
        field.sendKeys(password, Keys.ENTER);
    }

    /**
     * Wait for successful login.
     * Allows sufficient time for manual CAPTCHA and OTP.
     */
    public boolean waitUntilLoggedIn(int seconds) {

        long endTime = System.currentTimeMillis()
                + (seconds * 1000L);

        while (System.currentTimeMillis() < endTime) {

            try {

                if (isLoggedIn()) {
                    return true;
                }

                Thread.sleep(1000);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
                return false;
            }
        }

        return isLoggedIn();
    }

    public boolean isLoggedIn() {

        return !profileNameFromHeader().isEmpty();
    }

    /**
     * Extract the account greeting from the Amazon header.
     */
    public String profileNameFromHeader() {

        try {

            WebElement greeting = driver.findElement(
                    HEADER_GREETING
            );

            if (!greeting.isDisplayed()) {
                return "";
            }

            String text = greeting.getText().trim();

            if (text.isEmpty()
                    || text.toLowerCase().contains("sign in")) {
                return "";
            }

            return text.replaceFirst(
                    "(?i)^hello,?\\s*", ""
            ).trim();

        } catch (Exception e) {

            return "";
        }
    }
}
