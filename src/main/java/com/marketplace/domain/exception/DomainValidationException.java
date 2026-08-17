package com.marketplace.domain.exception;

/**
 * Exception thrown when a core business rule or domain constraint is violated.
 */
public class DomainValidationException extends RuntimeException {
    
    /**
     * Constructs a new DomainValidationException with the specified detail message.
     *
     * @param message The detail message explaining the reason for the validation failure.
     */
    public DomainValidationException(String message) {
        super(message);
    }
}
