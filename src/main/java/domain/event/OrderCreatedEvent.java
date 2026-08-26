package domain.event;

import java.time.LocalDateTime;

/**
 * Domain Event triggered when a new Order is successfully created in the system.
 */
public class OrderCreatedEvent implements DomainEvent {

    private final Long orderId;
    private final Long buyerId;
    private final LocalDateTime occurredOn;

    /**
     * Constructs a new OrderCreatedEvent.
     *
     * @param orderId The ID of the created order.
     * @param buyerId The ID of the buyer who placed the order.
     */
    public OrderCreatedEvent(Long orderId, Long buyerId) {
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.occurredOn = LocalDateTime.now();
    }

    /**
     * Retrieves the ID of the created order.
     *
     * @return The order ID.
     */
    public Long getOrderId() {
        return orderId;
    }

    /**
     * Retrieves the ID of the buyer.
     *
     * @return The buyer ID.
     */
    public Long getBuyerId() {
        return buyerId;
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
