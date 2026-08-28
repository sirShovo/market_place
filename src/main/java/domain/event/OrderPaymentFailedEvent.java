package domain.event;

import java.time.LocalDateTime;

/**
 * Domain Event triggered when an Order payment attempt is rejected or fails.
 */
public class OrderPaymentFailedEvent implements DomainEvent {

    private final Long orderId;
    private final String reason;
    private final LocalDateTime occurredOn;

    /**
     * Constructs a new OrderPaymentFailedEvent.
     *
     * @param orderId The ID of the order that failed payment.
     * @param reason  The reason for the payment failure.
     */
    public OrderPaymentFailedEvent(Long orderId, String reason) {
        this.orderId = orderId;
        this.reason = reason;
        this.occurredOn = LocalDateTime.now();
    }

    /**
     * Retrieves the ID of the order.
     *
     * @return The order ID.
     */
    public Long getOrderId() {
        return orderId;
    }

    /**
     * Retrieves the reason for the payment failure.
     *
     * @return The failure reason.
     */
    public String getReason() {
        return reason;
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
