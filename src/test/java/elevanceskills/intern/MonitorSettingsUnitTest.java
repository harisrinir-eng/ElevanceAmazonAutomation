package elevanceskills.intern;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MonitorSettingsUnitTest {

    private static Map<String, String> valid() {
        Map<String, String> m = new HashMap<>();
        m.put("task6.productUrl", "https://www.amazon.in/dp/B000TESTAA");
        m.put("task6.priceThreshold", "1000");
        m.put("task6.pollIntervalSeconds", "900");
        m.put("monitor.minIntervalSeconds", "60");
        m.put("task6.headless", "true");
        return m;
    }

    private static void assertRejected(Map<String, String> config, String expectedFragment) {
        try {
            MonitorSettings.from(config::get);
            Assert.fail("Expected a configuration error containing: " + expectedFragment);
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains(expectedFragment), e.getMessage());
        }
    }

    @Test(groups = "unit")
    public void validConfigurationIsAccepted() {
        MonitorSettings s = MonitorSettings.from(valid()::get);
        Assert.assertEquals(s.intervalSeconds(), 900);
        Assert.assertEquals(s.stopFile().toString(), "stop-monitor.flag");
    }

    @Test(groups = "unit")
    public void placeholderUrlIsRejected() {
        Map<String, String> m = valid();
        m.put("task6.productUrl", "https://www.amazon.in/dp/REPLACE_WITH_ASIN");
        assertRejected(m, "placeholder");
    }

    @Test(groups = "unit")
    public void badThresholdAndIntervalAreRejected() {
        Map<String, String> m = valid();
        m.put("task6.priceThreshold", "-5");
        assertRejected(m, "priceThreshold");

        m = valid();
        m.put("task6.pollIntervalSeconds", "10");
        assertRejected(m, "below the allowed minimum");

        m = valid();
        m.remove("task6.productUrl");
        assertRejected(m, "productUrl");
    }
}
