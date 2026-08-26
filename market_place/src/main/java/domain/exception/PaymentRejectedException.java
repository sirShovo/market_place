package domain.exception;
public class PaymentRejectedException extends RuntimeException {
    public PaymentRejectedException(String message) { super(message); }
}
