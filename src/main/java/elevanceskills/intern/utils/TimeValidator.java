package elevanceskills.intern.utils;

import java.time.Clock;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Checks whether "now" (in IST, Asia/Kolkata) lies inside a task's execution window.
 * Windows are half-open intervals [start, end): start is allowed, end is not.
 * A {@link Clock} can be injected so the logic is testable without waiting for real time.
 */
public final class TimeValidator {

    public static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    /** A named half-open time window [start, end) on a single day. */
    public record Window(String name, LocalTime start, LocalTime end) {
        public Window {
            if (!start.isBefore(end)) {
                throw new IllegalArgumentException("Window start must be before end: " + name);
            }
        }

        public boolean contains(LocalTime time) {
            return !time.isBefore(start) && time.isBefore(end);
        }

        @Override
        public String toString() {
            return name + " [" + start + ", " + end + ") IST";
        }
    }

    /** Tasks 1 and 2: 15:00 inclusive to 18:00 exclusive. */
    public static final Window TASKS_1_AND_2 = new Window("Tasks 1 & 2", LocalTime.of(15, 0), LocalTime.of(18, 0));
    /** Tasks 3 and 5: 18:00 inclusive to 19:00 exclusive. */
    public static final Window TASKS_3_AND_5 = new Window("Tasks 3 & 5", LocalTime.of(18, 0), LocalTime.of(19, 0));
    /** Task 4: 12:00 inclusive to 15:00 exclusive. */
    public static final Window TASK_4 = new Window("Task 4", LocalTime.of(12, 0), LocalTime.of(15, 0));
    // Task 6 has no window by the assignment, so there is deliberately no constant for it.

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Clock clock;

    /** Uses the real system clock. */
    public TimeValidator() {
        this(Clock.system(IST));
    }

    /** Uses the supplied clock (for deterministic tests). */
    public TimeValidator(Clock clock) {
        this.clock = clock;
    }

    public ZonedDateTime nowIst() {
        return ZonedDateTime.now(clock.withZone(IST));
    }

    public boolean isAllowed(Window window) {
        return window.contains(nowIst().toLocalTime());
    }

    /** Human-readable explanation of the current check, for logs and skip messages. */
    public String describe(Window window) {
        ZonedDateTime now = nowIst();
        String verdict = window.contains(now.toLocalTime()) ? "inside" : "OUTSIDE";
        return "Current IST time " + FORMAT.format(now) + " is " + verdict + " the permitted window " + window;
    }
}
