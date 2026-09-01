package application.domain.event;

import java.time.LocalDateTime;

/**
 * Domain Event triggered when an Order is successfully paid.
 */
public class OrderPaidEvent implements DomainEvent {

    private final Long orderId;
    private final LocalDateTime occurredOn;

    /**
     * Constructs a new OrderPaidEvent.
     *
     * @param orderId The ID of the paid order.
     */
    public OrderPaidEvent(Long orderId) {
        this.orderId = orderId;
        this.occurredOn = LocalDateTime.now();
    }

    /**
     * Retrieves the ID of the paid order.
     *
     * @return The order ID.
     */
    public Long getOrderId() {
        return orderId;
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
