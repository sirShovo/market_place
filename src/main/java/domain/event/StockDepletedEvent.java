package domain.event;

import java.time.LocalDateTime;

/**
 * Domain Event triggered when a product's stock in a specific warehouse reaches zero.
 */
public class StockDepletedEvent implements DomainEvent {

    private final Long productId;
    private final Long warehouseId;
    private final LocalDateTime occurredOn;

    /**
     * Constructs a new StockDepletedEvent.
     *
     * @param productId   The ID of the product.
     * @param warehouseId The ID of the warehouse where stock depleted.
     */
    public StockDepletedEvent(Long productId, Long warehouseId) {
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.occurredOn = LocalDateTime.now();
    }

    /**
     * Retrieves the product ID.
     *
     * @return The product ID.
     */
    public Long getProductId() {
        return productId;
    }

    /**
     * Retrieves the warehouse ID.
     *
     * @return The warehouse ID.
     */
    public Long getWarehouseId() {
        return warehouseId;
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
