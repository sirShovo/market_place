package application.domain.exceptions;

/**
 * Raised when a required domain entity cannot be found through an output port.
 */
public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String entity) {
        super(entity + " not found.");
    }
}
