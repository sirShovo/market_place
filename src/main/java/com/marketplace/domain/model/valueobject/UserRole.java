package com.marketplace.domain.model.valueobject;

/**
 * Defines the roles a user can have within the Market Place.
 * Used for Role-Based Access Control (RBAC).
 */
public enum UserRole {
    /** Administrator of the platform with global privileges. */
    ADMIN,
    
    /** A merchant or seller who can publish and manage products. */
    SELLER,
    
    /** A customer who browses the catalog and places orders. */
    BUYER
}
