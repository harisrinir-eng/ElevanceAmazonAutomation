
package elevanceskills.intern;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

import elevanceskills.intern.SearchResultsPage.ResultItem;
import elevanceskills.intern.utils.ConfigUtil;
import elevanceskills.intern.utils.PriceUtil;
import elevanceskills.intern.utils.TimeValidator;
import elevanceskills.intern.utils.ValidationUtil;

/**
 * Task 2 - Search, apply filters, and independently verify product details.
 *
 * Requirements:
 * 1. Brand starts with C.
 * 2. Price strictly greater than INR 2,000.
 * 3. Rating strictly greater than 4.0.
 *
 * Execution window: 15:00-18:00 IST.
 */
public class Test2 extends BaseTest {

    @Test(
        groups = {"browser", "task2"},
        description = "Task 2: brand, price and rating filters"
    )
    public void searchAndFilterProducts() {

        runTask(TimeValidator.TASKS_1_AND_2, this::task2);
    }

    private void task2() {

        // Read configuration values.
        String keyword =
                ConfigUtil.require("task2.searchKeyword");

        String initialText =
                ConfigUtil.require("task2.brandInitial").trim();

        Assert.assertFalse(
                initialText.isEmpty(),
                "Brand initial must not be empty."
        );

        BigDecimal minPrice =
                ConfigUtil.getDecimal("task2.minPriceExclusive");

        double minRating =
                ConfigUtil.getDouble("task2.minRatingExclusive");

        int maxScan =
                ConfigUtil.getInt("task2.maxResultsToScan");

        int maxOpen =
                ConfigUtil.getInt("task2.maxProductsToOpen");

        char initial = Character.toUpperCase(initialText.charAt(0));

        Assert.assertTrue(maxScan > 0,
                "Maximum search results to scan must be greater than zero.");

        Assert.assertTrue(maxOpen > 0,
                "Maximum product pages to open must be greater than zero.");

        log("========== TASK 2 STARTED ==========");
        log("Search keyword: " + keyword);
        log("Required brand initial: " + initial);
        log("Required price: Greater than INR " + minPrice);
        log("Required rating: Greater than " + minRating);

        // Open Amazon and search for the requested products.
        SearchResultsPage results =
                homePage().open(baseUrl()).search(keyword);

        /*
         * Do not select the first C-brand filter automatically.
         * Amazon may select a brand such as Campus, which can unnecessarily
         * restrict the available products.
         *
         * Verify the actual brand independently on each product page.
         */
        log("Brand filter: Product-page verification will be used "
                + "for all brands starting with " + initial);

        /*
         * Amazon's minimum-price filter was unavailable previously.
         * Verify the actual product-page price instead.
         */
        log("Price filter: Actual product-page price will be verified.");

        /*
         * Apply Amazon's 4-star-and-up filter.
         * Final validation independently checks rating > 4.0.
         */
        boolean ratingApplied =
                results.applyRatingFilter((int) Math.floor(minRating));

        if (ratingApplied) {
            log("Rating filter APPLIED: "
                    + (int) Math.floor(minRating) + " stars & up");
        } else {
            log("Rating filter NOT AVAILABLE - "
                    + "rating will be verified on product pages.");
        }

        // Read search-result cards.
        List<ResultItem> items = results.getResults(maxScan);

        Assert.assertFalse(
                items.isEmpty(),
                "No readable search-result cards found for: " + keyword
        );

        log("Read " + items.size() + " product cards.");

        List<String> rejections = new ArrayList<>();

        int opened = 0;
        int verified = 0;

        // Examine candidate products.
        for (ResultItem item : items) {

            if (opened >= maxOpen) {
                log("Maximum product-page limit reached: " + maxOpen);
                break;
            }

            /*
             * Important:
             * Do not reject candidates using search-card price or rating.
             * Search cards may show starting prices, variant prices,
             * incomplete ratings, or other values.
             *
             * Verify all three requirements on the actual product page.
             */

            opened++;

            log("------------------------------------");
            log("Opening candidate " + opened + ": " + item.title());
            log("ASIN: " + item.asin());

            ProductPage page = productPage();

            // Open the actual product page.
            if (!page.open(item.url())) {

                String reason =
                        item.asin() + " -> product page did not load";

                rejections.add(reason);
                log("Candidate rejected: " + reason);

                continue;
            }

            // Extract actual product details.
            Optional<String> pageBrand = page.brand();

            Optional<BigDecimal> pagePrice = page.price();

            Optional<Double> pageRating = page.rating();

            // Check whether all required details are available.
            if (pageBrand.isEmpty()
                    || pagePrice.isEmpty()
                    || pageRating.isEmpty()) {

                String reason =
                        item.asin()
                        + " -> brand/price/rating could not be verified";

                rejections.add(reason);

                log("Candidate rejected: " + reason);

                continue;
            }

            verified++;

            String actualBrand = pageBrand.get();

            BigDecimal actualPrice = pagePrice.get();

            double actualRating = pageRating.get();

            // Independently validate each requirement.
            boolean brandMatch =
                    ValidationUtil.startsWithAnyLetter(
                            actualBrand,
                            String.valueOf(initial)
                    );

            boolean priceMatch =
                    PriceUtil.isGreaterThan(
                            actualPrice,
                            minPrice
                    );

            boolean ratingMatch =
                    actualRating > minRating;

            // Display verification results.
            log("Product verification: " + page.title());

            log("Brand: " + actualBrand
                    + " -> " + (brandMatch ? "PASS" : "FAIL"));

            log("Price: INR " + actualPrice
                    + " -> " + (priceMatch ? "PASS" : "FAIL"));

            log("Rating: " + actualRating
                    + " -> " + (ratingMatch ? "PASS" : "FAIL"));

            // All three conditions must pass.
            if (brandMatch && priceMatch && ratingMatch) {

                log("------------------------------------");
                log("QUALIFYING PRODUCT FOUND!");

                log("Product: " + page.title());

                log("Verified brand: " + actualBrand);

                log("Verified price: INR " + actualPrice);

                log("Verified rating: " + actualRating);

                log("Product ASIN: " + item.asin());

                log("Total product pages opened: " + opened);

                log("Total products with complete details: " + verified);

                log("========== TASK 2 PASSED ==========");

                return;
            }

            // Record products that do not satisfy every requirement.
            String reason =
                    item.asin()
                    + " -> brand=" + actualBrand
                    + ", price=INR " + actualPrice
                    + ", rating=" + actualRating;

            rejections.add(reason);

            log("Candidate does not satisfy all requirements.");
        }

        // No qualifying product was found within the configured limits.
        log("========== TASK 2 FAILED ==========");

        Assert.fail(
                "No product satisfied all Task 2 requirements."
                + " Brand starts with '" + initial + "'"
                + ", price > INR " + minPrice
                + ", rating > " + minRating
                + ". Product pages opened: " + opened
                + ", products with complete details: " + verified
                + ". Rejection details: " + rejections
        );
    }
}
