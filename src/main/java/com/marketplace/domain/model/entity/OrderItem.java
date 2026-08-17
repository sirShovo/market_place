package com.marketplace.domain.model.entity;

import com.marketplace.domain.exception.DomainValidationException;
import com.marketplace.domain.model.valueobject.Money;

/**
 * Domain entity representing an individual item within an Order.
 * 
 * It belongs to the Order aggregate. It encapsulates the product ID, 
 * the purchased quantity, the unit price at the time of purchase, 
 * and automatically calculates its subtotal.
 */
public class OrderItem {
    private Long id;
    private Long productId;
    private int quantity;
    private Money unitPrice;
    private Money subtotal;

    /**
     * Default constructor for JPA and Mappers. Should not be used directly for domain logic.
     */
    protected OrderItem() {}

    private OrderItem(Long productId, int quantity, Money unitPrice) {
        if (productId == null) {
            throw new DomainValidationException("Product ID is required for order item");
        }
        if (quantity <= 0) {
            throw new DomainValidationException("Quantity must be greater than zero");
        }
        if (unitPrice == null) {
            throw new DomainValidationException("Unit price is required");
        }
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = unitPrice.multiply(quantity);
    }

    /**
     * Factory method to create an OrderItem.
     *
     * @param productId The ID of the product being purchased.
     * @param quantity  The amount of items purchased.
     * @param unitPrice The price of a single item at the time of purchase.
     * @return A new OrderItem entity instance.
     */
    public static OrderItem create(Long productId, int quantity, Money unitPrice) {
        return new OrderItem(productId, quantity, unitPrice);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public Money getUnitPrice() { return unitPrice; }
    public Money getSubtotal() { return subtotal; }
}
