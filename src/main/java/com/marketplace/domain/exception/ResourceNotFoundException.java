package com.marketplace.domain.exception;

/**
 * Exception thrown when an expected domain entity or aggregate cannot be found
 * in the underlying persistence mechanism.
 */
public class ResourceNotFoundException extends RuntimeException {
    
    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     *
     * @param message The detail message explaining what resource was not found.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
