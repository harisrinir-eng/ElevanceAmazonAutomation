
package elevanceskills.intern;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PriceMonitorAlertTest {

    private MonitorSettings createSettings() {
        return new MonitorSettings(
                "https://www.amazon.in/dp/B0H4Z69BSF",
                new BigDecimal("1000"),
                900,
                true,
                Path.of("stop-monitor-test.flag"));
    }

    @Test(description = "Verify price threshold and duplicate alert handling")
    public void shouldSendAlertsOnlyForQualifyingPriceChanges() {

        List<BigDecimal> sentAlerts = new ArrayList<>();

        PriceMonitor monitor = new PriceMonitor(
                createSettings(),
                sentAlerts::add);

        // Price is above threshold: no alert.
        Assert.assertFalse(
                monitor.processPrice(new BigDecimal("1200")));

        // First price below threshold: send alert.
        Assert.assertTrue(
                monitor.processPrice(new BigDecimal("999")));

        // Same price: do not send a duplicate alert.
        Assert.assertFalse(
                monitor.processPrice(new BigDecimal("999")));

        // Further reduction: send another alert.
        Assert.assertTrue(
                monitor.processPrice(new BigDecimal("950")));

        // Price recovers above threshold: reset alert tracking.
        Assert.assertFalse(
                monitor.processPrice(new BigDecimal("1100")));

        // Price drops below threshold again: send a new alert.
        Assert.assertTrue(
                monitor.processPrice(new BigDecimal("990")));

        Assert.assertEquals(sentAlerts.size(), 3);
        Assert.assertEquals(
                sentAlerts.get(0),
                new BigDecimal("999"));
        Assert.assertEquals(
                sentAlerts.get(1),
                new BigDecimal("950"));
        Assert.assertEquals(
                sentAlerts.get(2),
                new BigDecimal("990"));
    }

    @Test(description = "Verify failed email submission can be retried")
    public void shouldRetryWhenEmailSubmissionFails() {

        AtomicInteger attempts = new AtomicInteger();

        PriceMonitor monitor = new PriceMonitor(
                createSettings(),
                price -> {
                    if (attempts.incrementAndGet() == 1) {
                        throw new Exception("Simulated SMTP failure");
                    }
                });

        // First attempt fails.
        Assert.assertFalse(
                monitor.processPrice(new BigDecimal("900")));

        // Same price is retried because the first alert was not accepted.
        Assert.assertTrue(
                monitor.processPrice(new BigDecimal("900")));

        Assert.assertEquals(attempts.get(), 2);
    }
}
