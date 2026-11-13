package elevanceskills.intern.utils;

import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Pure (browser-free) validation rules used by the tasks. */
public final class ValidationUtil {

    /** Task 5 rule: exactly 10 ASCII letters/digits, no spaces or special characters. */
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9]{10}$");

    /**
     * Task 1 electronics rule (documented, rule-based, not perfect): a product is treated as
     * electronic when its title contains one of these whole words/phrases, or when its category
     * breadcrumb mentions an electronics category (see {@link #isElectronicsCategory}).
     */
    private static final List<String> ELECTRONIC_KEYWORDS = List.of(
            "phone", "smartphone", "mobile", "laptop", "tablet", "ipad", "headphone", "headphones",
            "earphone", "earphones", "earbuds", "headset", "charger", "usb", "hdmi", "bluetooth",
            "wireless", "smartwatch", "smart watch", "camera", "tv", "television", "speaker",
            "soundbar", "monitor", "keyboard", "mouse", "power bank", "powerbank", "router",
            "printer", "led", "battery", "batteries", "drone", "console", "playstation", "xbox",
            "pendrive", "pen drive", "hard disk", "ssd", "memory card", "sd card", "electronic",
            "electric", "trimmer", "shaver", "hair dryer", "projector", "gps", "fitness band");

    private static final Pattern ELECTRONIC_PATTERN = Pattern.compile(
            "\\b(?:" + ELECTRONIC_KEYWORDS.stream().map(Pattern::quote).collect(Collectors.joining("|")) + ")\\b",
            Pattern.CASE_INSENSITIVE);

    private ValidationUtil() {
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME.matcher(username).matches();
    }

    /** Returns the (upper-case) letters from {@code forbidden} that occur in {@code text}, case-insensitively. */
    public static SortedSet<Character> forbiddenLettersIn(String text, String forbidden) {
        SortedSet<Character> found = new TreeSet<>();
        if (text == null || forbidden == null) {
            return found;
        }
        String forbiddenUpper = forbidden.toUpperCase(Locale.ROOT);
        for (char c : text.toUpperCase(Locale.ROOT).toCharArray()) {
            if (forbiddenUpper.indexOf(c) >= 0) {
                found.add(c);
            }
        }
        return found;
    }

    /** True if the first non-blank character of {@code text} is one of {@code letters}, ignoring case. */
    public static boolean startsWithAnyLetter(String text, String letters) {
        if (text == null || letters == null) {
            return false;
        }
        String trimmed = text.strip();
        if (trimmed.isEmpty()) {
            return false;
        }
        char first = Character.toUpperCase(trimmed.charAt(0));
        return letters.toUpperCase(Locale.ROOT).indexOf(first) >= 0;
    }

    public static boolean isLikelyElectronic(String title) {
        return title != null && ELECTRONIC_PATTERN.matcher(title).find();
    }

    public static boolean isElectronicsCategory(String breadcrumb) {
        if (breadcrumb == null) {
            return false;
        }
        String b = breadcrumb.toLowerCase(Locale.ROOT);
        return b.contains("electronics") || b.contains("mobiles") || b.contains("computers & accessories");
    }

    /** "Visit the Campus Store" / "Brand: Campus" -> "Campus". */
    public static String cleanBrand(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.strip();
        s = s.replaceFirst("(?i)^visit the\\s+", "");
        s = s.replaceFirst("(?i)^brand:\\s*", "");
        s = s.replaceFirst("(?i)\\s+store$", "");
        return s.strip();
    }

    /** Loose title comparison (search card vs product page vs cart line); compares the first 25 letters/digits. */
    public static boolean titlesMatch(String a, String b) {
        String na = normalise(a);
        String nb = normalise(b);
        if (na.isEmpty() || nb.isEmpty()) {
            return false;
        }
        int n = Math.min(25, Math.min(na.length(), nb.length()));
        return na.substring(0, n).equals(nb.substring(0, n));
    }

    private static String normalise(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
