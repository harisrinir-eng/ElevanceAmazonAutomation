package elevanceskills.intern.utils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Reads config.properties (working directory, falling back to the classpath).
 * Precedence: -Dkey=value system property > config.local.properties > config.properties.
 * Secrets are read from environment variables via {@link #env(String)}, never from files.
 */
public final class ConfigUtil {

    private static Properties props;

    private ConfigUtil() {
    }

    private static synchronized Properties props() {
        if (props == null) {
            Properties p = new Properties();
            boolean loaded = loadFile(p, Path.of("config.properties"));
            if (!loaded) {
                try (InputStream in = ConfigUtil.class.getResourceAsStream("/config.properties")) {
                    if (in != null) {
                        p.load(in);
                        loaded = true;
                    }
                } catch (IOException ignored) {
                    // handled below
                }
            }
            if (!loaded) {
                throw new IllegalStateException("config.properties was not found in the working directory ("
                        + Path.of("").toAbsolutePath() + "). Run Maven from the project root.");
            }
            loadFile(p, Path.of("config.local.properties")); // optional local overrides
            props = p;
        }
        return props;
    }

    private static boolean loadFile(Properties target, Path file) {
        if (!Files.isRegularFile(file)) {
            return false;
        }
        try (InputStream in = Files.newInputStream(file)) {
            target.load(in);
            return true;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + file + ": " + e.getMessage(), e);
        }
    }

    /** Value or null if absent/blank. */
    public static String get(String key) {
        String v = System.getProperty(key);
        if (v == null) {
            v = props().getProperty(key);
        }
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    public static String require(String key) {
        String v = get(key);
        if (v == null) {
            throw new IllegalStateException("Missing required setting '" + key + "' in config.properties");
        }
        return v;
    }

    public static int getInt(String key) {
        String v = require(key);
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Setting '" + key + "' must be a whole number but is '" + v + "'");
        }
    }

    public static double getDouble(String key) {
        String v = require(key);
        try {
            return Double.parseDouble(v);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Setting '" + key + "' must be a number but is '" + v + "'");
        }
    }

    public static BigDecimal getDecimal(String key) {
        String v = require(key);
        try {
            return new BigDecimal(v);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Setting '" + key + "' must be a number but is '" + v + "'");
        }
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(require(key));
    }

    /** Comma-separated list, blanks removed. */
    public static List<String> getList(String key) {
        List<String> out = new ArrayList<>();
        Arrays.stream(require(key).split(",")).map(String::trim).filter(s -> !s.isEmpty()).forEach(out::add);
        return out;
    }

    /** Fails once with ALL missing keys listed. */
    public static void requireKeys(String... keys) {
        List<String> missing = new ArrayList<>();
        for (String k : keys) {
            if (get(k) == null) {
                missing.add(k);
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing required settings in config.properties: " + missing);
        }
    }

    /** Environment variable or null if unset/blank. Used for credentials and secrets. */
    public static String env(String name) {
        String v = System.getenv(name);
        return (v == null || v.isBlank()) ? null : v.trim();
    }
}
