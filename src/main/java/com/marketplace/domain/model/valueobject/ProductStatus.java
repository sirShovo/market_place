package com.marketplace.domain.model.valueobject;

/**
 * Defines the operational states of a Product within the catalog.
 */
public enum ProductStatus {
    /** The product is available for purchase. */
    ACTIVE,
    
    /** The product's stock quantity has reached zero. */
    OUT_OF_STOCK,
    
    /** The product was manually deactivated by the seller or an admin. */
    INACTIVE
}
