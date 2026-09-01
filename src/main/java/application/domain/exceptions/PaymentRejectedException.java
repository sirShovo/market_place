package application.domain.exceptions;

/**
 * Raised when the payment gateway rejects the payment of an order. The buyer may retry
 * (spec Domain 7, model operative flow step 6).
 */
public class PaymentRejectedException extends DomainException {

    public PaymentRejectedException(String message) {
        super(message);
    }
}
