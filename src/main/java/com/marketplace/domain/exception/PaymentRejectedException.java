package com.marketplace.domain.exception;

/**
 * Exception thrown when a payment processing attempt fails or is rejected.
 */
public class PaymentRejectedException extends RuntimeException {
    
    /**
     * Constructs a new PaymentRejectedException with the specified detail message.
     *
     * @param message The detail message explaining the reason for the payment rejection.
     */
    public PaymentRejectedException(String message) {
        super(message);
    }
}
