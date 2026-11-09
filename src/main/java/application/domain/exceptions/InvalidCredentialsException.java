package application.domain.exceptions;

/**
 * Raised when authentication fails due to missing, unknown, inactive, or invalid credentials.
 */
public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Invalid username or password.");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
