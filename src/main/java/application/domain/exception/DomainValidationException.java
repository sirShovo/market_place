package application.domain.exception;

/**
 * Custom domain exception for DomainValidationException.
 */
public class DomainValidationException extends RuntimeException {
    /**
     * Constructor for DomainValidationException.
     * @param message The error message.
     */
    public DomainValidationException(String message) { super(message); }
}
