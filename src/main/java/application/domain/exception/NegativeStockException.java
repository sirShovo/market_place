package application.domain.exception;

/**
 * Custom domain exception for NegativeStockException.
 */
public class NegativeStockException extends RuntimeException {
    /**
     * Constructor for NegativeStockException.
     * @param message The error message.
     */
    public NegativeStockException(String message) { super(message); }
}
