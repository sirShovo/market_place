package application.domain.exceptions;

/**
 * Raised when a registration would break the platform-wide uniqueness of the document
 * identifier or the e-mail address (spec §11).
 */
public class DuplicateUserException extends DomainException {

    public DuplicateUserException(String message) {
        super(message);
    }
}
