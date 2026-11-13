package elevanceskills.intern.utils;

import java.math.BigDecimal;

/**
 * Decides when Task 6 should send an e-mail, so the same unchanged price drop is never reported twice.
 * <ul>
 *   <li>price below threshold and nothing sent yet for this drop -> notify</li>
 *   <li>price drops even further than the last notified price -> notify again</li>
 *   <li>price unchanged / higher (but still below threshold) -> no repeat</li>
 *   <li>price back at or above threshold -> state reset, so a later drop notifies again</li>
 * </ul>
 * {@link #markNotified} must only be called after the SMTP server accepted the message.
 */
public final class PriceAlertTracker {

    private final BigDecimal threshold;
    private BigDecimal lastNotifiedPrice;

    public PriceAlertTracker(BigDecimal threshold) {
        this.threshold = threshold;
    }

    public boolean shouldNotify(BigDecimal currentPrice) {
        if (currentPrice == null) {
            return false;
        }
        if (currentPrice.compareTo(threshold) >= 0) {
            lastNotifiedPrice = null; // drop is over; a future drop is a new event
            return false;
        }
        return lastNotifiedPrice == null || currentPrice.compareTo(lastNotifiedPrice) < 0;
    }

    public void markNotified(BigDecimal price) {
        this.lastNotifiedPrice = price;
    }
}
