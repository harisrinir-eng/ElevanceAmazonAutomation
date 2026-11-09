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

/**
 * Task 6 - separate entry point (NOT part of the TestNG suite).
 * Polls a product price and e-mails when it drops below the threshold.
 * Stop it with Ctrl+C or by creating the stop file (default: stop-monitor.flag).
 *
 * Run: mvn compile exec:java
 */
public final class PriceMonitor {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MonitorSettings settings;
    private final EmailUtil email;
    private final PriceAlertTracker tracker;
    private volatile boolean stopRequested;
    private WebDriver driver;

    PriceMonitor(MonitorSettings settings, EmailUtil email) {
        this.settings = settings;
        this.email = email;
        this.tracker = new PriceAlertTracker(settings.threshold());
    }

    public static void main(String[] args) {
        MonitorSettings settings;
        SmtpSettings smtp;
        try {
            settings = MonitorSettings.fromConfig();
            smtp = SmtpSettings.fromEnv();
        } catch (IllegalStateException e) {
            System.err.println("Cannot start price monitor. " + e.getMessage());
            System.exit(1);
            return;
        }
        new PriceMonitor(settings, new EmailUtil(smtp)).run();
    }

    private void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> stopRequested = true));
        log("Monitoring " + settings.productUrl() + " | notify below INR " + settings.threshold()
                + " | every " + settings.intervalSeconds() + "s | stop: Ctrl+C or create '" + settings.stopFile() + "'");
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
            Optional<BigDecimal> price = readPrice(driver, settings.productUrl());
            if (price.isEmpty()) {
                log("Price not available on the page right now (out of stock or layout change). No notification.");
                return;
            }
            log("Current price: INR " + price.get());
            if (tracker.shouldNotify(price.get())) {
                sendAlert(price.get());
            }
        } catch (RuntimeException e) {
            log("Could not read the price (network error, CAPTCHA or browser problem): " + e.getMessage());
            quitDriver(); // start a fresh browser on the next poll
        }
    }

    private void sendAlert(BigDecimal price) {
        String subject = "Price drop: now INR " + price + " (threshold INR " + settings.threshold() + ")";
        String body = "The price fell below your threshold.\n\nCurrent price: INR " + price
                + "\nThreshold: INR " + settings.threshold() + "\nProduct: " + settings.productUrl()
                + "\nChecked at: " + STAMP.format(new TimeValidator().nowIst()) + " IST";
        try {
            email.send(subject, body);
            tracker.markNotified(price); // only after the SMTP server accepted the message
            log("E-mail SUBMITTED to the SMTP server (acceptance by the server, not proof of inbox delivery).");
        } catch (Exception e) {
            log("SMTP error - e-mail NOT sent, will retry on the next poll: " + e.getMessage());
        }
    }

    /** Opens the product page and reads the actual displayed price once. */
    public static Optional<BigDecimal> readPrice(WebDriver driver, String productUrl) {
        int wait = ConfigUtil.get("explicitWait.seconds") == null ? 20 : ConfigUtil.getInt("explicitWait.seconds");
        ProductPage page = new ProductPage(driver, new WebDriverWait(driver, Duration.ofSeconds(wait)), 0);
        if (!page.open(productUrl)) {
            return Optional.empty();
        }
        return page.price();
    }

    private boolean shouldStop() {
        return stopRequested || Files.exists(settings.stopFile());
    }

    private void sleepUntilNextPoll() {
        for (int i = 0; i < settings.intervalSeconds() && !shouldStop(); i++) {
            try {
                Thread.sleep(1000); // 1-second slices so a stop request is noticed quickly
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
                // browser already gone
            }
            driver = null;
        }
    }

    private static void log(String message) {
        System.out.println("[" + STAMP.format(ZonedDateTime.now(TimeValidator.IST)) + " IST] " + message);
    }
}
