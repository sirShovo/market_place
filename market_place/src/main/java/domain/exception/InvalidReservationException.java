package domain.exception;

/**
 * Custom domain exception for InvalidReservationException.
 */
public class InvalidReservationException extends RuntimeException {
    /**
     * Constructor for InvalidReservationException.
     * @param message The error message.
     */
    public InvalidReservationException(String message) { super(message); }
}
