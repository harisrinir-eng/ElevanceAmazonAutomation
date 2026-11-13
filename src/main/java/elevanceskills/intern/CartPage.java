package elevanceskills.intern;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import elevanceskills.intern.utils.PriceUtil;

/** Shopping cart: line items, displayed subtotal, and the (safe) Proceed to Checkout click. */
public class CartPage extends BasePage {

    public record CartItem(String asin, String title, BigDecimal price, int quantity) {
    }

    private static final By ITEM = By.cssSelector(
            "#activeCartViewForm div.sc-list-item[data-asin], div[data-name='Active Items'] div.sc-list-item[data-asin]");
    private static final By SUBTOTAL = By.cssSelector(
            "#sc-subtotal-amount-activecart .sc-price, #sc-subtotal-amount-buybox .sc-price, #sc-subtotal-amount-activecart");

    public CartPage(WebDriver driver, WebDriverWait wait, int manualWaitSeconds) {
        super(driver, wait, manualWaitSeconds);
    }

    public CartPage open(String baseUrl) {
        driver.get(baseUrl + "/gp/cart/view.html");
        handleCaptcha();
        try {
            wait.until(d -> exists(ITEM) || exists(By.id("sc-empty-cart")) || exists(By.cssSelector(".sc-your-amazon-cart-is-empty")));
        } catch (TimeoutException e) {
            // an unreadable cart is reported by the caller (no items found)
        }
        return this;
    }

    public List<CartItem> items() {
        List<CartItem> items = new ArrayList<>();
        for (WebElement row : driver.findElements(ITEM)) {
            String title = "";
            List<WebElement> titles = row.findElements(By.cssSelector(".sc-product-title, span.a-truncate-full"));
            if (!titles.isEmpty()) {
                title = text(titles.get(0));
            }
            String rawPrice = row.getDomAttribute("data-price");
            if (rawPrice == null || rawPrice.isBlank()) {
                List<WebElement> prices = row.findElements(By.cssSelector(".sc-product-price"));
                rawPrice = prices.isEmpty() ? "" : text(prices.get(0));
            }
            BigDecimal price = PriceUtil.parsePrice(rawPrice).orElse(null);
            int qty = 1;
            String rawQty = row.getDomAttribute("data-quantity");
            if (rawQty != null && rawQty.matches("\\d+")) {
                qty = Integer.parseInt(rawQty);
            }
            items.add(new CartItem(row.getDomAttribute("data-asin"), title, price, qty));
        }
        return items;
    }

    /** Subtotal shown by Amazon, if readable. */
    public Optional<BigDecimal> displayedSubtotal() {
        for (WebElement e : driver.findElements(SUBTOTAL)) {
            Optional<BigDecimal> parsed = PriceUtil.parsePrice(text(e));
            if (parsed.isPresent()) {
                return parsed;
            }
        }
        return Optional.empty();
    }

    /** Sum of price x quantity over the readable line items (cross-check for the displayed subtotal). */
    public BigDecimal computedTotal(List<CartItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            if (item.price() != null) {
                total = total.add(item.price().multiply(BigDecimal.valueOf(item.quantity())));
            }
        }
        return total;
    }

    /**
     * Clicks "Proceed to checkout" and returns the URL reached. This only navigates; the framework never
     * clicks anything on checkout or payment pages.
     */
    public String proceedToCheckout() {
        List<WebElement> buttons = driver.findElements(By.cssSelector("input[name='proceedToRetailCheckout']"));
        if (buttons.isEmpty()) {
            return "";
        }
        String before = driver.getCurrentUrl();
        click(buttons.get(0));
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> !d.getCurrentUrl().equals(before));
        } catch (TimeoutException ignored) {
            // stay on the cart page; caller reports it
        }
        return driver.getCurrentUrl();
    }
}
