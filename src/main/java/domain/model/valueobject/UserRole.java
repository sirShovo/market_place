package domain.model.valueobject;

/**
 * Enumeration representing UserRole states/types.
 */
public enum UserRole {
    /** Administrator with full access */
    ADMIN,
    /** Seller managing their catalog */
    SELLER,
    /** Customer purchasing items */
    BUYER,
    /** Logistics operator managing physical warehouses */
    LOGISTICS,
    /** Supervisor with read access */
    SUPERVISOR
}
