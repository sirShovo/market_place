package com.marketplace.domain.exception;

/**
 * Exception thrown when an attempt is made to reserve or deduct more stock
 * than is currently available for a given product.
 */
public class InsufficientStockException extends RuntimeException {
    
    /**
     * Constructs a new InsufficientStockException with the specified detail message.
     *
     * @param message The detail message explaining the stock discrepancy.
     */
    public InsufficientStockException(String message) {
        super(message);
    }
}
