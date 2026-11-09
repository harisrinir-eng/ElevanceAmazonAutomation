package elevanceskills.intern;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

import elevanceskills.intern.SearchResultsPage.ResultItem;
import elevanceskills.intern.utils.ConfigUtil;
import elevanceskills.intern.utils.TimeValidator;
import elevanceskills.intern.utils.ValidationUtil;

/**
 * Task 1 - select a non-electronic product whose title does not begin with A, B, C or D,
 * open it and validate title and price. Window: 15:00-18:00 IST.
 */
public class Test1 extends BaseTest {

    @Test(groups = {"browser", "task1"},
            description = "Task 1: qualifying product selection and product-page validation")
    public void selectQualifyingProductAndValidateProductPage() {
        runTask(TimeValidator.TASKS_1_AND_2, this::task1);
    }

    private void task1() {
        String keyword = ConfigUtil.require("task1.searchKeyword");
        int maxScan = ConfigUtil.getInt("task1.maxResultsToScan");
        int maxOpen = ConfigUtil.getInt("task1.maxProductsToOpen");

        List<ResultItem> items = homePage().open(baseUrl()).search(keyword).getResults(maxScan);
        Assert.assertFalse(items.isEmpty(), "No search results were readable for '" + keyword + "'");
        log("Read " + items.size() + " result cards for '" + keyword + "'");

        List<String> rejections = new ArrayList<>();
        int opened = 0;
        for (ResultItem item : items) {
            if (opened >= maxOpen) {
                break;
            }
            String reason = rejectionFromSearchCard(item);
            if (reason != null) {
                rejections.add(shorten(item.title()) + " -> " + reason);
                continue;
            }
            opened++;
            ProductPage page = productPage();
            if (!page.open(item.url())) {
                rejections.add(shorten(item.title()) + " -> product page did not load");
                continue;
            }
            String title = page.title();
            Optional<BigDecimal> price = page.price();
            String pageReason = rejectionFromProductPage(title, price, page.breadcrumb());
            if (pageReason != null) {
                rejections.add(shorten(title) + " -> " + pageReason);
                continue;
            }
            log("SELECTED product: " + title);
            log("Price on product page: INR " + price.get());
            Assert.assertFalse(ValidationUtil.startsWithAnyLetter(title, "ABCD"), "Title must not begin with A-D");
            Assert.assertTrue(price.get().signum() > 0, "Product price must be positive");
            return;
        }
        Assert.fail("No qualifying product found for '" + keyword + "' (opened " + opened + " product pages). "
                + "Rejected: " + rejections);
    }

    private static String rejectionFromSearchCard(ResultItem item) {
        if (item.title().isBlank()) {
            return "no title";
        }
        if (ValidationUtil.startsWithAnyLetter(item.title(), "ABCD")) {
            return "title begins with A, B, C or D";
        }
        if (ValidationUtil.isLikelyElectronic(item.title())) {
            return "looks like an electronic product (title keyword)";
        }
        return null;
    }

    private static String rejectionFromProductPage(String title, Optional<BigDecimal> price, String breadcrumb) {
        if (title.isBlank()) {
            return "product page has no title";
        }
        if (ValidationUtil.startsWithAnyLetter(title, "ABCD")) {
            return "page title begins with A, B, C or D";
        }
        if (ValidationUtil.isLikelyElectronic(title) || ValidationUtil.isElectronicsCategory(breadcrumb)) {
            return "electronics (title keyword or category breadcrumb)";
        }
        if (price.isEmpty()) {
            return "no price displayed (unavailable)";
        }
        return null;
    }

    private static String shorten(String s) {
        return s.length() > 60 ? s.substring(0, 60) + "..." : s;
    }
}
