package application.domain.exceptions;

/**
 * Raised when an entity is asked to move to a status that is not reachable from its
 * current one.
 */
public class InvalidStatusTransitionException extends DomainException {

    public InvalidStatusTransitionException(String from, String to) {
        super("Invalid status transition: " + from + " -> " + to + ".");
    }
}
