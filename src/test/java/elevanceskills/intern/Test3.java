
package elevanceskills.intern;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

import elevanceskills.intern.CartPage.CartItem;
import elevanceskills.intern.SearchResultsPage.ResultItem;
import elevanceskills.intern.utils.ConfigUtil;
import elevanceskills.intern.utils.PriceUtil;
import elevanceskills.intern.utils.TimeValidator;
import elevanceskills.intern.utils.ValidationUtil;

/**
 * Task 3 - search, add to cart, verify cart amount and test checkout.
 *
 * SAFETY:
 * No real Amazon order is placed and no payment details are entered.
 *
 * Method 1:
 * Search Amazon, add a product, verify its ASIN in the cart,
 * validate cart amount and stop at the checkout boundary.
 *
 * Method 2:
 * Perform payment and order confirmation on a local MOCK page.
 */
public class Test3 extends BaseTest {

    private BigDecimal liveCartAmount;
    private String liveProductTitle;

    private static final Pattern ASIN_PATTERN = Pattern.compile(
            "(?:/dp/|/gp/product/)([A-Z0-9]{10})",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ASIN_QUERY_PATTERN = Pattern.compile(
            "[?&]asin=([A-Z0-9]{10})",
            Pattern.CASE_INSENSITIVE
    );

    @Test(
        groups = {"browser", "task3"},
        description = "Task 3 (live Amazon): cart amount check, stops at checkout boundary"
    )
    public void liveCartAndCheckoutBoundary() {
        runTask(TimeValidator.TASKS_3_AND_5, this::liveSteps);
    }

    @Test(
        groups = {"browser", "task3"},
        dependsOnMethods = "liveCartAndCheckoutBoundary",
        description = "Task 3 (MOCK checkout): payment and order confirmation - not an Amazon order"
    )
    public void mockCheckoutPaymentAndOrderConfirmation() {
        runTask(TimeValidator.TASKS_3_AND_5, this::mockSteps);
    }

    private void liveSteps() {

        String keyword = ConfigUtil.require("task3.searchKeyword");

        BigDecimal minAmount =
                ConfigUtil.getDecimal("task3.minAmountExclusive");

        int maxTries =
                ConfigUtil.getInt("task3.maxProductsToTry");

        List<ResultItem> items = homePage()
                .open(baseUrl())
                .search(keyword)
                .getResults(30);

        Assert.assertFalse(
                items.isEmpty(),
                "No search results readable for '" + keyword + "'"
        );

        String addedTitle = null;
        String addedAsin = null;
        BigDecimal pagePrice = null;

        int tries = 0;

        for (ResultItem item : items) {

            if (tries >= maxTries) {
                break;
            }

            if (item.price() == null
                    || !PriceUtil.isGreaterThan(item.price(), minAmount)) {
                continue;
            }

            tries++;

            ProductPage page = productPage();

            if (!page.open(item.url())) {
                continue;
            }

            // First obtain ASIN from the search result URL.
            String candidateAsin = extractAsin(item.url());

            // Fallback: obtain ASIN from the actual product-page URL.
            if (candidateAsin == null || candidateAsin.isBlank()) {
                candidateAsin = extractAsin(driver.getCurrentUrl());
            }

            Optional<BigDecimal> price = page.price();

            if (price.isPresent()
                    && PriceUtil.isGreaterThan(price.get(), minAmount)
                    && page.addToCart()) {

                addedTitle = page.title();
                addedAsin = candidateAsin;
                pagePrice = price.get();

                // Final fallback in case the result URL did not expose the ASIN.
                if (addedAsin == null || addedAsin.isBlank()) {
                    addedAsin = extractAsin(driver.getCurrentUrl());
                }

                break;
            }
        }

        Assert.assertNotNull(
                addedTitle,
                "Could not add any product priced above INR " + minAmount
                        + " to the cart"
        );

        log("Added to cart: " + addedTitle
                + " (page price INR " + pagePrice + ")");

        log("Selected product ASIN: "
                + (addedAsin == null ? "Unavailable" : addedAsin));

        // Open Amazon cart.
        CartPage cart = cartPage().open(baseUrl());

        List<CartItem> cartItems = cart.items();

        log("Cart items extracted: " + cartItems);

        final String expectedTitle = addedTitle;
        final String expectedAsin = addedAsin;

        // Primary matching: exact ASIN.
        boolean asinMatch = expectedAsin != null
                && !expectedAsin.isBlank()
                && cartItems.stream().anyMatch(item ->
                    item.asin() != null
                    && item.asin().equalsIgnoreCase(expectedAsin)
                );

        // Fallback matching: normalized product title.
        boolean titleMatch = cartItems.stream().anyMatch(item ->
                item.title() != null
                && !item.title().isBlank()
                && ValidationUtil.titlesMatch(
                        item.title(),
                        expectedTitle
                )
        );

        boolean inCart = asinMatch || titleMatch;

        log("Cart verification - ASIN match: " + asinMatch);
        log("Cart verification - Title match: " + titleMatch);

        Assert.assertTrue(
                inCart,
                "Selected product was not found in the cart."
                        + " Expected ASIN: " + expectedAsin
                        + ", Expected title: " + expectedTitle
                        + ", Cart lines: " + cartItems
        );

        log("SUCCESS: Selected product verified in Amazon cart.");

        // Read displayed subtotal or calculate from cart line items.
        BigDecimal amount = cart.displayedSubtotal()
                .orElseGet(() -> cart.computedTotal(cartItems));

        log("Cart amount read from Amazon: INR " + amount);

        Assert.assertTrue(
                PriceUtil.isGreaterThan(amount, minAmount),
                "Cart amount INR " + amount
                        + " must be strictly greater than INR " + minAmount
        );

        liveCartAmount = amount;
        liveProductTitle = addedTitle;

        // Proceed only to the checkout boundary.
        // Never enter payment details or place a live order.
        String reached = cart.proceedToCheckout();

        if (reached.isEmpty()) {

            log("LIVE CHECKOUT NOT REACHED: "
                    + "'Proceed to checkout' button not found."
                    + " Live order confirmation was NOT performed.");

        } else if (reached.contains("signin")
                || reached.contains("/ap/")) {

            log("LIVE CHECKOUT STOPPED at Amazon sign-in boundary ("
                    + reached
                    + "). No payment made; live order confirmation was NOT performed.");

        } else {

            log("LIVE CHECKOUT STOPPED at " + reached
                    + ". No payment made; live order confirmation was NOT performed.");
        }
    }

    /**
     * Extracts a 10-character Amazon ASIN from a product URL.
     */
    private String extractAsin(String url) {

        if (url == null || url.isBlank()) {
            return null;
        }

        Matcher matcher = ASIN_PATTERN.matcher(url);

        if (matcher.find()) {
            return matcher.group(1).toUpperCase();
        }

        matcher = ASIN_QUERY_PATTERN.matcher(url);

        if (matcher.find()) {
            return matcher.group(1).toUpperCase();
        }

        return null;
    }

    private void mockSteps() {

        Assert.assertNotNull(
                liveCartAmount,
                "No live cart amount available from the previous step"
        );

        driver.get(mockCheckoutUrl(
                liveCartAmount,
                liveProductTitle
        ));

        Assert.assertTrue(
                driver.findElement(By.id("banner"))
                        .getText()
                        .contains("MOCK"),
                "Mock banner must be visible"
        );

        Optional<BigDecimal> shown = PriceUtil.parsePrice(
                driver.findElement(By.id("amount")).getText()
        );

        Assert.assertTrue(
                shown.isPresent()
                        && shown.get().compareTo(liveCartAmount) == 0,
                "Mock checkout amount must equal the real cart amount INR "
                        + liveCartAmount
        );

        wait.until(d ->
                d.findElement(By.id("mock-pay-button")).isEnabled()
        );

        driver.findElement(By.id("mock-pay-button")).click();

        wait.until(d ->
                d.findElement(By.id("confirmation")).isDisplayed()
        );

        String status = driver.findElement(
                By.id("confirmation-status")
        ).getText();

        String orderId = driver.findElement(
                By.id("order-id")
        ).getText();

        Assert.assertTrue(
                status.contains("MOCK ORDER CONFIRMED"),
                "Unexpected confirmation text: " + status
        );

        Assert.assertTrue(
                orderId.startsWith("MOCK-"),
                "Order id must be labelled MOCK: " + orderId
        );

        log("MOCK ORDER TESTED: " + orderId
                + " for INR " + liveCartAmount
                + ". This is NOT an Amazon order; no live Amazon order was placed.");
    }

    private String mockCheckoutUrl(
            BigDecimal amount,
            String product
    ) {

        try {

            var resource = getClass().getResource(
                    "/mock-checkout/checkout.html"
            );

            Assert.assertNotNull(
                    resource,
                    "mock-checkout/checkout.html is missing from test resources"
            );

            URI base = resource.toURI();

            return base
                    + "?amount="
                    + URLEncoder.encode(
                            amount.toPlainString(),
                            StandardCharsets.UTF_8
                    )
                    + "&product="
                    + URLEncoder.encode(
                            product,
                            StandardCharsets.UTF_8
                    );

        } catch (URISyntaxException e) {

            throw new IllegalStateException(
                    "Bad mock checkout path",
                    e
            );
        }
    }
}
