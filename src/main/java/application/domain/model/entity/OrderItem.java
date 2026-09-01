package application.domain.model.entity;
import application.domain.model.valueobject.Money;

/**
 * Domain entity representing an individual item within an Order.
 */
public class OrderItem {
    private Long id;
    private Long productId;
    private int quantity;
    private Money unitPrice;
    
    /**
     * Constructor for OrderItem.
     * @param productId The ID of the purchased product.
     * @param quantity The amount purchased.
     * @param unitPrice The unit price at the time of purchase.
     */
    public OrderItem(Long productId, int quantity, Money unitPrice) {
        this.productId = productId; this.quantity = quantity; this.unitPrice = unitPrice;
    }
    
    /** @return The OrderItem ID */
    public Long getId() { return id; }
    /** @param id The OrderItem ID to set */
    public void setId(Long id) { this.id = id; }
    
    /** @return The product ID */
    public Long getProductId() { return productId; }
    
    /** 
     * Calculates the subtotal for this item.
     * @return The subtotal as a Money object.
     */
    public Money getSubtotal() { return unitPrice.multiply(quantity); }
}
