
package elevanceskills.intern;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import elevanceskills.intern.utils.BrowserFactory;
import elevanceskills.intern.utils.ConfigUtil;
import elevanceskills.intern.utils.EmailUtil;
import elevanceskills.intern.utils.EmailUtil.SmtpSettings;
import elevanceskills.intern.utils.PriceAlertTracker;
import elevanceskills.intern.utils.TimeValidator;

public final class PriceMonitor {

    private static final DateTimeFormatter STAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MonitorSettings settings;
    private final AlertSender alertSender;
    private final PriceAlertTracker tracker;

    private volatile boolean stopRequested;
    private WebDriver driver;

    @FunctionalInterface
    interface AlertSender {
        void send(BigDecimal price) throws Exception;
    }

    PriceMonitor(MonitorSettings settings, EmailUtil email) {
        this(settings, price -> {
            String subject = "Price alert: INR " + price
                    + " (threshold INR " + settings.threshold() + ")";

            String body = "The current product price is below your configured threshold."
                    + "\n\nCurrent price: INR " + price
                    + "\nThreshold: INR " + settings.threshold()
                    + "\nProduct: " + settings.productUrl()
                    + "\nChecked at: " + STAMP.format(new TimeValidator().nowIst())
                    + " IST";

            email.send(subject, body);
        });
    }

    // Used by automated tests to verify the alert workflow without sending real emails.
    PriceMonitor(MonitorSettings settings, AlertSender alertSender) {
        this.settings = settings;
        this.alertSender = alertSender;
        this.tracker = new PriceAlertTracker(settings.threshold());
    }

    public static void main(String[] args) {
        try {
            MonitorSettings settings = MonitorSettings.fromConfig();
            SmtpSettings smtp = SmtpSettings.fromEnv();

            new PriceMonitor(settings, new EmailUtil(smtp)).run();

        } catch (IllegalStateException e) {
            System.err.println("Cannot start price monitor: " + e.getMessage());
            System.exit(1);
        }
    }

    private void run() {
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> stopRequested = true));

        log("Price monitor started.");
        log("Product: " + settings.productUrl());
        log("Alert threshold: INR " + settings.threshold());
        log("Polling interval: " + settings.intervalSeconds() + " seconds");
        log("Stop using Ctrl+C or create: " + settings.stopFile());

        try {
            while (!shouldStop()) {
                pollOnce();
                sleepUntilNextPoll();
            }
        } finally {
            quitDriver();
            log("Price monitor stopped.");
        }
    }

    private void pollOnce() {
        try {
            if (driver == null) {
                driver = BrowserFactory.createChrome(settings.headless());
            }

            Optional<BigDecimal> price =
                    readPrice(driver, settings.productUrl());

            if (price.isEmpty()) {
                log("Price unavailable. No alert generated.");
                return;
            }

            log("Current product price: INR " + price.get());
            log("Configured threshold: INR " + settings.threshold());

            processPrice(price.get());

        } catch (BlockedException e) {
            log("BLOCKED: Amazon requires CAPTCHA or manual verification. "
                    + "The monitor will retry during the next polling cycle. "
                    + e.getMessage());
            quitDriver();

        } catch (RuntimeException e) {
            log("Price check failed due to a browser, network, or page error: "
                    + e.getMessage());
            quitDriver();
        }
    }

    /**
     * Evaluates a price and sends an alert when the tracker identifies
     * a qualifying price below the configured threshold.
     *
     * Returns true only when the alert sender accepts the notification.
     */
    boolean processPrice(BigDecimal currentPrice) {
        if (currentPrice == null) {
            log("No valid price supplied. No alert generated.");
            return false;
        }

        if (!tracker.shouldNotify(currentPrice)) {
            log("No new price alert required.");
            return false;
        }

        try {
            alertSender.send(currentPrice);

            // Record the notification only after the sender succeeds.
            tracker.markNotified(currentPrice);

            log("PRICE ALERT SUBMITTED: INR " + currentPrice);
            return true;

        } catch (Exception e) {
            log("PRICE ALERT FAILED. The monitor will retry on a later poll: "
                    + e.getMessage());
            return false;
        }
    }

    /** Opens the product page and reads the displayed price once. */
    public static Optional<BigDecimal> readPrice(
            WebDriver driver, String productUrl) {

        String waitValue = ConfigUtil.get("explicitWait.seconds");

        int waitSeconds = waitValue == null
                ? 20
                : ConfigUtil.getInt("explicitWait.seconds");

        ProductPage page = new ProductPage(
                driver,
                new WebDriverWait(driver, Duration.ofSeconds(waitSeconds)),
                0);

        if (!page.open(productUrl)) {
            return Optional.empty();
        }

        return page.price();
    }

    private boolean shouldStop() {
        return stopRequested || Files.exists(settings.stopFile());
    }

    private void sleepUntilNextPoll() {
        for (int i = 0;
                i < settings.intervalSeconds() && !shouldStop();
                i++) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                stopRequested = true;
                return;
            }
        }
    }

    private void quitDriver() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (RuntimeException ignored) {
                // Browser may already have closed.
            }
            driver = null;
        }
    }

    private static void log(String message) {
        System.out.println(
                "[" + STAMP.format(ZonedDateTime.now(TimeValidator.IST))
                        + " IST] " + message);
    }
}
