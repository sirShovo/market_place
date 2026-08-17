package com.marketplace.domain.model.valueobject;

/**
 * Defines the lifecycle states of a User account.
 */
public enum UserStatus {
    /** The account is active and can perform operations. */
    ACTIVE,
    
    /** The account has been blocked manually by an administrator. */
    BLOCKED,
    
    /** The account is temporarily suspended. */
    SUSPENDED
}
