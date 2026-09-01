package application.domain.exceptions;

/**
 * Raised when any change is attempted on a finalized ({@code DELIVERED}) order
 * (spec §11).
 */
public class FinalizedOrderModificationException extends DomainException {

    public FinalizedOrderModificationException(String orderIdentifier) {
        super("Order " + orderIdentifier + " is finalized and cannot be modified.");
    }
}
