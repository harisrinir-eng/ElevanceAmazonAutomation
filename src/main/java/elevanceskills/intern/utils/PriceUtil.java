package elevanceskills.intern.utils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parsing helpers for prices and ratings as Amazon.in displays them. */
public final class PriceUtil {

    // First number in the text; commas (including Indian 1,29,999 grouping) are allowed.
    private static final Pattern AMOUNT = Pattern.compile("(\\d[\\d,]*(?:\\.\\d+)?)");
    private static final Pattern RATING = Pattern.compile("(\\d(?:[.,]\\d)?)");

    private PriceUtil() {
    }

    /** "\u20B91,299.00", "Rs. 2,499" or "\u20B91,29,999" -> BigDecimal. Empty if no number is present. */
    public static Optional<BigDecimal> parsePrice(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        Matcher m = AMOUNT.matcher(raw);
        if (!m.find()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new BigDecimal(m.group(1).replace(",", "")));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /** "4.3 out of 5 stars" -> 4.3. Empty if no value between 0 and 5 can be read. */
    public static Optional<Double> parseRating(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        Matcher m = RATING.matcher(raw);
        if (!m.find()) {
            return Optional.empty();
        }
        double value = Double.parseDouble(m.group(1).replace(',', '.'));
        return (value >= 0 && value <= 5) ? Optional.of(value) : Optional.empty();
    }

    /** Strict comparison: a &gt; b. */
    public static boolean isGreaterThan(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) > 0;
    }

    public static boolean isLessThan(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) < 0;
    }
}
