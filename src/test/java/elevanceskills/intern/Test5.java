package elevanceskills.intern;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import elevanceskills.intern.CartPage.CartItem;
import elevanceskills.intern.SearchResultsPage.ResultItem;
import elevanceskills.intern.utils.ConfigUtil;
import elevanceskills.intern.utils.PriceUtil;
import elevanceskills.intern.utils.TimeValidator;
import elevanceskills.intern.utils.ValidationUtil;

/**
 * Task 5 - add several products, verify the real cart total is strictly above INR 2,000 (no order is placed),
 * and validate a configurable test username (exactly 10 letters/digits). Window: 18:00-19:00 IST.
 */
public class Test5 extends BaseTest {

    @Test(groups = {"task5"}, description = "Task 5: username rule ^[A-Za-z0-9]{10}$ on the configured test value")
    public void usernameValidation() {
        // No browser needed, but the assignment restricts Task 5 to its time window, so it is enforced here too.
        runTaskWithoutBrowser();
    }

    @Test(groups = {"browser", "task5"}, description = "Task 5: multiple products and actual cart total")
    public void multipleProductsCartTotal() {
        runTask(TimeValidator.TASKS_3_AND_5, this::cartSteps);
    }

    private void runTaskWithoutBrowser() {
        if (!timeValidator.isAllowed(TimeValidator.TASKS_3_AND_5)) {
            String info = timeValidator.describe(TimeValidator.TASKS_3_AND_5);
            log("SKIPPED - not executed. " + info);
            throw new org.testng.SkipException("Not executed: " + info);
        }
        String username = ConfigUtil.require("task5.username");
        Assert.assertTrue(ValidationUtil.isValidUsername(username),
                "Test username '" + username + "' must be exactly 10 letters/digits with no spaces or special characters");
        log("Username '" + username + "' satisfies ^[A-Za-z0-9]{10}$ (configured test input, not the Amazon account name).");
    }

    private void cartSteps() {
        List<String> keywords = ConfigUtil.getList("task5.searchKeywords");
        BigDecimal minItemPrice = ConfigUtil.getDecimal("task5.minItemPrice");
        BigDecimal minTotal = ConfigUtil.getDecimal("task5.minTotalExclusive");
        int minProducts = ConfigUtil.getInt("task5.minProducts");

        AmazonHomePage home = homePage().open(baseUrl());
        List<String> added = new ArrayList<>();
        for (String keyword : keywords) {
            List<ResultItem> items = home.search(keyword).getResults(20);
            int tries = 0;
            for (ResultItem item : items) {
                if (tries >= 5) {
                    break;
                }
                if (item.price() == null || item.price().compareTo(minItemPrice) < 0) {
                    continue;
                }
                tries++;
                ProductPage page = productPage();
                if (page.open(item.url()) && page.addToCart()) {
                    added.add(page.title());
                    log("Added for '" + keyword + "': " + page.title());
                    break;
                }
            }
        }
        Assert.assertTrue(added.size() >= minProducts,
                "Only " + added.size() + " product(s) could be added; need at least " + minProducts + ". Added: " + added);

        CartPage cart = cartPage().open(baseUrl());
        List<CartItem> cartItems = cart.items();
        Assert.assertTrue(cartItems.size() >= minProducts,
                "Cart shows " + cartItems.size() + " line(s); expected at least " + minProducts);
        cartItems.forEach(i -> log("Cart line: " + i.title() + " | INR " + i.price() + " x " + i.quantity()));

        BigDecimal computed = cart.computedTotal(cartItems);
        BigDecimal total = cart.displayedSubtotal().orElse(computed);
        log("Cart subtotal displayed by Amazon: " + cart.displayedSubtotal().map(BigDecimal::toPlainString).orElse("not readable")
                + " | computed from lines: INR " + computed);
        Assert.assertTrue(total.signum() > 0, "Cart total could not be read");
        Assert.assertTrue(PriceUtil.isGreaterThan(total, minTotal),
                "Cart total INR " + total + " must be strictly greater than INR " + minTotal);
    }
}
