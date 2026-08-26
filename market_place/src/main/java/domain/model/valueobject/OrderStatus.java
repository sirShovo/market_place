package domain.model.valueobject;

/**
 * Enumeration representing OrderStatus states/types.
 */
public enum OrderStatus {
    /** In the buyer's cart */
    CART,
    /** Awaiting payment confirmation */
    PENDING_PAYMENT,
    /** Payment completed successfully */
    PAID,
    /** Physically shipped to buyer */
    SHIPPED,
    /** Arrived at destination */
    DELIVERED,
    /** Order was cancelled */
    CANCELLED
}
