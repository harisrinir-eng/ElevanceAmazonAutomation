package elevanceskills.intern;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import elevanceskills.intern.utils.TimeValidator;
import elevanceskills.intern.utils.TimeValidator.Window;

/** Half-open [start, end) boundaries, using a fixed clock (no waiting for real time, no network). */
public class TimeValidatorUnitTest {

    private static TimeValidator at(int h, int m, int s) {
        Instant instant = ZonedDateTime.of(2026, 10, 23, h, m, s, 0, TimeValidator.IST).toInstant();
        return new TimeValidator(Clock.fixed(instant, TimeValidator.IST));
    }

    @DataProvider(name = "boundaries")
    public Object[][] boundaries() {
        Window w12 = TimeValidator.TASK_4;
        Window w15 = TimeValidator.TASKS_1_AND_2;
        Window w18 = TimeValidator.TASKS_3_AND_5;
        return new Object[][] {
                // Task 4: 12:00 inclusive - 15:00 exclusive
                {w12, 11, 59, 59, false}, {w12, 12, 0, 0, true}, {w12, 14, 59, 59, true}, {w12, 15, 0, 0, false},
                // Tasks 1 & 2: 15:00 inclusive - 18:00 exclusive
                {w15, 14, 59, 59, false}, {w15, 15, 0, 0, true}, {w15, 17, 59, 59, true}, {w15, 18, 0, 0, false},
                // Tasks 3 & 5: 18:00 inclusive - 19:00 exclusive
                {w18, 17, 59, 59, false}, {w18, 18, 0, 0, true}, {w18, 18, 59, 59, true}, {w18, 19, 0, 0, false},
                {w18, 0, 0, 0, false}, {w15, 23, 59, 59, false}
        };
    }

    @Test(groups = "unit", dataProvider = "boundaries")
    public void windowBoundaries(Window window, int h, int m, int s, boolean expected) {
        Assert.assertEquals(at(h, m, s).isAllowed(window), expected, window + " at " + h + ":" + m + ":" + s);
    }

    @Test(groups = "unit")
    public void utcInstantIsConvertedToIst() {
        // 09:30 UTC == 15:00 IST (UTC+5:30)
        Clock utcClock = Clock.fixed(Instant.parse("2026-10-23T09:30:00Z"), java.time.ZoneOffset.UTC);
        TimeValidator validator = new TimeValidator(utcClock);
        Assert.assertEquals(validator.nowIst().getHour(), 15);
        Assert.assertTrue(validator.isAllowed(TimeValidator.TASKS_1_AND_2));
    }

    @Test(groups = "unit")
    public void describeMentionsVerdictAndWindow() {
        String text = at(10, 0, 0).describe(TimeValidator.TASKS_1_AND_2);
        Assert.assertTrue(text.contains("OUTSIDE") && text.contains("15:00") && text.contains("18:00"), text);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void invalidWindowIsRejected() {
        new Window("bad", java.time.LocalTime.of(10, 0), java.time.LocalTime.of(9, 0));
    }
}
