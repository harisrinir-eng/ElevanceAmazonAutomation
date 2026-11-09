
package elevanceskills.intern;

import java.math.BigDecimal;
import java.util.Optional;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import jakarta.mail.MessagingException;

import elevanceskills.intern.utils.BrowserFactory;
import elevanceskills.intern.utils.EmailUtil;
import elevanceskills.intern.utils.EmailUtil.SmtpSettings;
import elevanceskills.intern.utils.ScreenshotUtil;

public class Test6 {

    @Test(groups = {"task6"},
        description = "Task 6: product URL, threshold and interval are valid")
    public void monitorConfigurationIsValid() {

        try {
            MonitorSettings s = MonitorSettings.fromConfig();

            logLine(
                "Monitor config OK: threshold INR "
                + s.threshold()
                + ", every "
                + s.intervalSeconds() + "s"
            );

        } catch (IllegalStateException e) {

            throw new SkipException(
                "Task 6 not configured: " + e.getMessage()
            );
        }
    }

    @Test(groups = {"task6"},
        description = "Task 6: SMTP environment variables are present")
    public void smtpEnvironmentIsConfigured() {

        try {
            SmtpSettings s = SmtpSettings.fromEnv();

            logLine("SMTP settings present: " + s);

        } catch (IllegalStateException e) {

            throw new SkipException(
                "SMTP environment not configured: "
                + e.getMessage()
            );
        }
    }

    @Test(groups = {"task6"},
        dependsOnMethods = "smtpEnvironmentIsConfigured",
        description = "Task 6: send a test email through Brevo SMTP")
    public void sendTestEmailThroughBrevo() {

        SmtpSettings settings = SmtpSettings.fromEnv();

        EmailUtil emailUtil = new EmailUtil(settings);

        String subject = "Elevance Task 6 - SMTP Test";

        String body =
            "Hello,\n\n"
            + "This is a test email from the "
            + "ElevanceAmazonAutomation project.\n\n"
            + "SMTP Provider: Brevo\n"
            + "Purpose: Verify SMTP email notification.\n"
            + "Status: Test message submitted successfully.\n\n"
            + "This is an automated test message.";

        try {

            emailUtil.send(subject, body);

            logLine(
                "SUCCESS: Brevo SMTP accepted the test email "
                + "for submission to the configured recipient."
            );

        } catch (MessagingException e) {

            logLine(
                "SMTP EMAIL FAILED: "
                + e.getClass().getSimpleName()
                + " - "
                + e.getMessage()
            );

            throw new AssertionError(
                "Unable to submit test email through Brevo SMTP.",
                e
            );
        }
    }

    @Test(groups = {"browser", "task6"},
        dependsOnMethods = "monitorConfigurationIsValid",
        description = "Task 6: read the actual price once")
    public void readActualPriceOnce() {

        MonitorSettings s = MonitorSettings.fromConfig();

        boolean headless = s.headless();

        WebDriver driver =
            BrowserFactory.createChrome(headless);

        try {

            Optional<BigDecimal> price =
                PriceMonitor.readPrice(
                    driver,
                    s.productUrl()
                );

            if (price.isEmpty()) {

                ScreenshotUtil.capture(
                    driver,
                    "task6_price_unavailable"
                );

                throw new SkipException(
                    "Price not available on the page."
                );
            }

            logLine(
                "Actual price read from the page: INR "
                + price.get()
                + " (threshold INR "
                + s.threshold() + ")"
            );

            Assert.assertTrue(
                price.get().signum() > 0,
                "Price must be positive"
            );

        } catch (BlockedException e) {

            ScreenshotUtil.capture(driver, "task6_blocked");

            throw new SkipException(
                "BLOCKED (not passed): " + e.getMessage()
            );

        } finally {
            driver.quit();
        }
    }

    private static void logLine(String message) {
        org.testng.Reporter.log(message, true);
    }
}
