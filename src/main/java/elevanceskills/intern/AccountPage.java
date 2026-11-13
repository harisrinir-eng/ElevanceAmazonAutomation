package elevanceskills.intern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

/** "Your Account" and "Login &amp; security" pages (read-only). */
public class AccountPage extends BasePage {

    public AccountPage(WebDriver driver, WebDriverWait wait, int manualWaitSeconds) {
        super(driver, wait, manualWaitSeconds);
    }

    public AccountPage openYourAccount(String baseUrl) {
        driver.get(baseUrl + "/gp/css/homepage.html");
        handleCaptcha();
        return this;
    }

    public AccountPage openLoginAndSecurity(String baseUrl) {
        driver.get(baseUrl + "/ax/account/manage");
        handleCaptcha();
        return this;
    }

    /** Amazon may ask for the password again before showing account details. */
    public boolean requiresReauthentication() {
        return driver.getCurrentUrl().contains("/ap/signin") || exists(By.id("ap_password"));
    }

    public String pageText() {
        return text(driver.findElement(By.tagName("body")));
    }

    public boolean pageContainsName(String name) {
        return pageText().toLowerCase().contains(name.toLowerCase());
    }
}
