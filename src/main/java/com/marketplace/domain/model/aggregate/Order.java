package com.marketplace.domain.model.aggregate;

import com.marketplace.domain.exception.DomainValidationException;
import com.marketplace.domain.model.entity.OrderItem;
import com.marketplace.domain.model.valueobject.Money;
import com.marketplace.domain.model.valueobject.OrderStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Domain aggregate root representing a purchase Order.
 * 
 * Manages its own lifecycle and ensures that state transitions strictly follow
 * business rules (e.g., from CREATED to PENDING_PAYMENT to PAID). It encapsulates
 * a collection of OrderItem entities and automatically calculates the total amount.
 */
public class Order {
    private Long id;
    private Long buyerId;
    private List<OrderItem> items;
    private Money totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Default constructor for JPA and Mappers. Should not be used directly for domain logic.
     */
    protected Order() {}

    private Order(Long buyerId) {
        if (buyerId == null) throw new DomainValidationException("Buyer is required to create an order");
        this.buyerId = buyerId;
        this.items = new ArrayList<>();
        this.totalAmount = Money.zero();
        this.status = OrderStatus.CREATED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * Factory method to create a new Order.
     *
     * @param buyerId The ID of the user (BUYER) making the purchase.
     * @return A new Order aggregate root instance in the CREATED state.
     */
    public static Order create(Long buyerId) {
        return new Order(buyerId);
    }

    /**
     * Adds a new item to the order and updates the total amount.
     *
     * @param item The OrderItem to add.
     * @throws DomainValidationException If the order is no longer in the CREATED state.
     */
    public void addItem(OrderItem item) {
        if (this.status != OrderStatus.CREATED) {
            throw new DomainValidationException("Cannot add items to an order that is not in CREATED state");
        }
        this.items.add(item);
        this.totalAmount = this.totalAmount.add(item.getSubtotal());
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Transitions the order state to PENDING_PAYMENT.
     *
     * @throws DomainValidationException If the order has no items or is not in the CREATED state.
     */
    public void pendingPayment() {
        if (this.status != OrderStatus.CREATED) {
            throw new DomainValidationException("Invalid state transition to PENDING_PAYMENT");
        }
        if (this.items.isEmpty()) {
            throw new DomainValidationException("Order must have at least one item to proceed to payment");
        }
        this.status = OrderStatus.PENDING_PAYMENT;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Transitions the order state to PAID after successful payment processing.
     *
     * @throws DomainValidationException If the order is not currently in PENDING_PAYMENT.
     */
    public void markAsPaid() {
        if (this.status != OrderStatus.PENDING_PAYMENT) {
            throw new DomainValidationException("Order must be in PENDING_PAYMENT to be marked as PAID");
        }
        this.status = OrderStatus.PAID;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Cancels the order.
     *
     * @throws DomainValidationException If the order has already been shipped, delivered, or cancelled.
     */
    public void cancel() {
        if (this.status == OrderStatus.SHIPPED || this.status == OrderStatus.DELIVERED || this.status == OrderStatus.CANCELLED) {
            throw new DomainValidationException("Order cannot be cancelled in its current state");
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getBuyerId() { return buyerId; }
    
    /**
     * Retrieves an unmodifiable list of the order items.
     *
     * @return The list of items.
     */
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    
    public Money getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
