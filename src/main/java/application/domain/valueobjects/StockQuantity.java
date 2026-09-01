package application.domain.valueobjects;

import application.domain.exceptions.NegativeStockException;

/**
 * A non-negative amount of stock. Immutable (spec Domain 6, §11).
 */
public record StockQuantity(int value) {

    public StockQuantity {
        if (value < 0) {
            throw new NegativeStockException("Stock quantity cannot be negative.");
        }
    }

    public static StockQuantity of(int value) {
        return new StockQuantity(value);
    }

    public StockQuantity add(int quantity) {
        return new StockQuantity(this.value + quantity);
    }

    public StockQuantity subtract(int quantity) {
        if (quantity > this.value) {
            throw new NegativeStockException("Not enough stock.");
        }
        return new StockQuantity(this.value - quantity);
    }
}
