package domain.exception;

/**
 * Custom domain exception for PaymentRejectedException.
 */
public class PaymentRejectedException extends RuntimeException {
    /**
     * Constructor for PaymentRejectedException.
     * @param message The error message.
     */
    public PaymentRejectedException(String message) { super(message); }
}
