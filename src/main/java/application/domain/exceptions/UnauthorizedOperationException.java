package application.domain.exceptions;

/**
 * Raised when a user attempts an operation that its role or status does not allow
 * (spec RG01, RG03, responsibility matrix §12).
 */
public class UnauthorizedOperationException extends DomainException {

    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
