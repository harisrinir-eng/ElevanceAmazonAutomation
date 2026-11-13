package elevanceskills.intern;

import java.math.BigDecimal;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import elevanceskills.intern.utils.PriceUtil;

public class PriceUtilUnitTest {

    @DataProvider(name = "prices")
    public Object[][] prices() {
        return new Object[][] {
                {"\u20B91,299.00", "1299.00"}, {"\u20B9 2,499", "2499"}, {"Rs. 999", "999"},
                {"\u20B91,29,999.50", "129999.50"}, {"1299", "1299"}, {"\u20B9499\u20B9999", "499"},
                {"  \u20B9 2,000.00 ", "2000.00"}
        };
    }

    @Test(groups = "unit", dataProvider = "prices")
    public void parsesPrices(String raw, String expected) {
        Optional<BigDecimal> parsed = PriceUtil.parsePrice(raw);
        Assert.assertTrue(parsed.isPresent(), raw);
        Assert.assertEquals(parsed.get().compareTo(new BigDecimal(expected)), 0, raw + " -> " + parsed.get());
    }

    @Test(groups = "unit")
    public void unreadablePricesAreEmpty() {
        Assert.assertTrue(PriceUtil.parsePrice(null).isEmpty());
        Assert.assertTrue(PriceUtil.parsePrice("").isEmpty());
        Assert.assertTrue(PriceUtil.parsePrice("Currently unavailable").isEmpty());
    }

    @Test(groups = "unit")
    public void parsesRatings() {
        Assert.assertEquals(PriceUtil.parseRating("4.3 out of 5 stars").get(), 4.3);
        Assert.assertEquals(PriceUtil.parseRating("4 out of 5 stars").get(), 4.0);
        Assert.assertEquals(PriceUtil.parseRating("4,5").get(), 4.5);
        Assert.assertTrue(PriceUtil.parseRating("no rating").isEmpty());
        Assert.assertTrue(PriceUtil.parseRating("9 out of 5").isEmpty());
        Assert.assertTrue(PriceUtil.parseRating(null).isEmpty());
    }

    @Test(groups = "unit")
    public void strictComparisons() {
        BigDecimal limit = new BigDecimal("2000");
        Assert.assertFalse(PriceUtil.isGreaterThan(new BigDecimal("2000.00"), limit), "2000 is NOT > 2000");
        Assert.assertTrue(PriceUtil.isGreaterThan(new BigDecimal("2000.01"), limit));
        Assert.assertTrue(PriceUtil.isLessThan(new BigDecimal("999.99"), new BigDecimal("1000")));
        Assert.assertFalse(PriceUtil.isLessThan(new BigDecimal("1000"), new BigDecimal("1000")));
    }
}
