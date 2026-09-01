package application.domain.exceptions;

/**
 * Raised when a reservation targets non-existent or {@code DAMAGED} inventory, or an
 * otherwise invalid quantity (spec §11).
 */
public class InvalidReservationException extends DomainException {

    public InvalidReservationException(String message) {
        super(message);
    }
}
