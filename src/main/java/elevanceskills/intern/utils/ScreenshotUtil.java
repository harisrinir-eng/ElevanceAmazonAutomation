package elevanceskills.intern.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/** Saves screenshots under target/screenshots/. Never throws: a failed screenshot must not hide the real failure. */
public final class ScreenshotUtil {

    private static final Path DIR = Path.of("target", "screenshots");

    private ScreenshotUtil() {
    }

    /** @return the saved file, or null if the screenshot could not be taken */
    public static Path capture(WebDriver driver, String label) {
        if (!(driver instanceof TakesScreenshot shooter)) {
            return null;
        }
        try {
            Files.createDirectories(DIR);
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path file = DIR.resolve(label.replaceAll("[^A-Za-z0-9_-]", "_") + "_" + stamp + ".png");
            Files.write(file, shooter.getScreenshotAs(OutputType.BYTES));
            return file;
        } catch (IOException | RuntimeException e) {
            System.err.println("Could not save screenshot: " + e.getMessage());
            return null;
        }
    }
}
