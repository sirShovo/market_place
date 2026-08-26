package domain.model.valueobject;
import domain.exception.DomainValidationException;
import domain.exception.NegativeStockException;
public class StockQuantity {
    private final int value;
    public StockQuantity(int value) {
        if (value < 0) throw new NegativeStockException("Stock quantity cannot be negative");
        this.value = value;
    }
    public static StockQuantity of(int v) { return new StockQuantity(v); }
    public StockQuantity add(int quantity) { return new StockQuantity(this.value + quantity); }
    public StockQuantity subtract(int quantity) {
        if (this.value < quantity) throw new NegativeStockException("Not enough stock");
        return new StockQuantity(this.value - quantity);
    }
    public int getValue() { return value; }
}
