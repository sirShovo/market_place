package application.domain.model.valueobject;
import application.domain.exception.DomainValidationException;
import application.domain.exception.NegativeStockException;

/**
 * Value Object representing a quantity of stock.
 * Ensures that stock cannot be negative.
 */
public class StockQuantity {
    private final int value;
    
    /**
     * Constructor for StockQuantity.
     * @param value The integer amount of stock.
     */
    public StockQuantity(int value) {
        if (value < 0) throw new NegativeStockException("Stock quantity cannot be negative");
        this.value = value;
    }
    
    /**
     * Factory method for StockQuantity.
     * @param v The integer amount.
     * @return StockQuantity instance.
     */
    public static StockQuantity of(int v) { return new StockQuantity(v); }
    
    /**
     * Adds quantity to the stock.
     * @param quantity The amount to add.
     * @return A new StockQuantity with the sum.
     */
    public StockQuantity add(int quantity) { return new StockQuantity(this.value + quantity); }
    
    /**
     * Subtracts quantity from the stock.
     * @param quantity The amount to subtract.
     * @return A new StockQuantity with the difference.
     * @throws NegativeStockException If the operation results in a negative value.
     */
    public StockQuantity subtract(int quantity) {
        if (this.value < quantity) throw new NegativeStockException("Not enough stock");
        return new StockQuantity(this.value - quantity);
    }
    
    /**
     * Gets the integer stock value.
     * @return The stock value.
     */
    public int getValue() { return value; }
}
