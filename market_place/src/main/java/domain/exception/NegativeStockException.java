package domain.exception;
public class NegativeStockException extends RuntimeException {
    public NegativeStockException(String message) { super(message); }
}
