package elevanceskills.intern;

import java.math.BigDecimal;

import org.testng.Assert;
import org.testng.annotations.Test;

import elevanceskills.intern.utils.PriceAlertTracker;

public class PriceAlertTrackerUnitTest {

    private static BigDecimal p(String v) {
        return new BigDecimal(v);
    }

    @Test(groups = "unit")
    public void notifiesOnceForUnchangedDrop() {
        PriceAlertTracker t = new PriceAlertTracker(p("1000"));
        Assert.assertFalse(t.shouldNotify(p("1200")), "above threshold");
        Assert.assertFalse(t.shouldNotify(p("1000")), "equal to threshold is not below");
        Assert.assertTrue(t.shouldNotify(p("950")), "first drop");
        t.markNotified(p("950"));
        Assert.assertFalse(t.shouldNotify(p("950")), "same price again");
        Assert.assertFalse(t.shouldNotify(p("980")), "price rose but still below threshold");
    }

    @Test(groups = "unit")
    public void notifiesAgainOnFurtherDropOrNewDrop() {
        PriceAlertTracker t = new PriceAlertTracker(p("1000"));
        t.markNotified(p("950"));
        Assert.assertTrue(t.shouldNotify(p("900")), "deeper drop");
        t.markNotified(p("900"));
        Assert.assertFalse(t.shouldNotify(p("1100")), "recovered above threshold resets state");
        Assert.assertTrue(t.shouldNotify(p("990")), "new drop after recovery");
    }

    @Test(groups = "unit")
    public void failedSendIsRetried() {
        PriceAlertTracker t = new PriceAlertTracker(p("1000"));
        Assert.assertTrue(t.shouldNotify(p("900")));
        // SMTP failed -> markNotified NOT called -> still due on the next poll
        Assert.assertTrue(t.shouldNotify(p("900")));
    }

    @Test(groups = "unit")
    public void unavailablePriceNeverNotifies() {
        Assert.assertFalse(new PriceAlertTracker(p("1000")).shouldNotify(null));
    }
}
