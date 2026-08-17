package com.marketplace.domain.model.valueobject;

/**
 * Defines the lifecycle states of an Order within the checkout process.
 */
public enum OrderStatus {
    /** The order is just created and items can still be modified. */
    CREATED,
    
    /** The order is finalized and waiting for the payment confirmation. */
    PENDING_PAYMENT,
    
    /** The payment was successfully processed. */
    PAID,
    
    /** The seller is currently processing/preparing the items. */
    PROCESSING,
    
    /** The order has been dispatched for delivery. */
    SHIPPED,
    
    /** The buyer has received the order successfully. */
    DELIVERED,
    
    /** The order was cancelled either manually or automatically (e.g., due to payment timeout). */
    CANCELLED
}
