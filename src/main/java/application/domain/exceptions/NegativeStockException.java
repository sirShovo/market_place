package application.domain.exceptions;

/**
 * Raised when an inventory operation would drive stock below zero (spec Domain 6, §11).
 */
public class NegativeStockException extends DomainException {

    public NegativeStockException(String message) {
        super(message);
    }
}
