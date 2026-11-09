package elevanceskills.intern;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import elevanceskills.intern.utils.ConfigUtil;

/** Validated Task 6 settings. {@link #from} takes any key lookup so validation is testable offline. */
public record MonitorSettings(String productUrl, BigDecimal threshold, int intervalSeconds, boolean headless, Path stopFile) {

    public static MonitorSettings fromConfig() {
        return from(ConfigUtil::get);
    }

    public static MonitorSettings from(Function<String, String> lookup) {
        List<String> problems = new ArrayList<>();

        String url = lookup.apply("task6.productUrl");
        if (url == null || !url.startsWith("http")) {
            problems.add("task6.productUrl must be an http(s) URL");
        } else if (url.contains("REPLACE_WITH")) {
            problems.add("task6.productUrl is still the placeholder - put a real Amazon.in product URL in config.properties");
        }

        BigDecimal threshold = null;
        try {
            threshold = new BigDecimal(String.valueOf(lookup.apply("task6.priceThreshold")));
            if (threshold.signum() <= 0) {
                problems.add("task6.priceThreshold must be greater than 0");
            }
        } catch (NumberFormatException e) {
            problems.add("task6.priceThreshold must be a number");
        }

        int minInterval = 60;
        String minRaw = lookup.apply("monitor.minIntervalSeconds");
        if (minRaw != null && minRaw.matches("\\d+")) {
            minInterval = Integer.parseInt(minRaw);
        }
        int interval = 0;
        String intervalRaw = lookup.apply("task6.pollIntervalSeconds");
        if (intervalRaw == null || !intervalRaw.matches("\\d+")) {
            problems.add("task6.pollIntervalSeconds must be a whole number of seconds");
        } else {
            interval = Integer.parseInt(intervalRaw);
            if (interval < minInterval) {
                problems.add("task6.pollIntervalSeconds (" + interval + ") is below the allowed minimum of "
                        + minInterval + " seconds (avoids excessive requests to Amazon)");
            }
        }

        String stop = lookup.apply("task6.stopFile");
        boolean headless = Boolean.parseBoolean(String.valueOf(lookup.apply("task6.headless")));

        if (!problems.isEmpty()) {
            throw new IllegalStateException("Invalid Task 6 configuration: " + problems);
        }
        return new MonitorSettings(url, threshold, interval, headless,
                Path.of(stop == null ? "stop-monitor.flag" : stop));
    }
}
