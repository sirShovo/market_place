package domain.exception;

/**
 * Custom domain exception for ResourceNotFoundException.
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
     * Constructor for ResourceNotFoundException.
     * @param message The error message.
     */
    public ResourceNotFoundException(String message) { super(message); }
}
