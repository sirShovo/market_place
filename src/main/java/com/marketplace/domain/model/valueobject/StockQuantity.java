package com.marketplace.domain.model.valueobject;

import com.marketplace.domain.exception.DomainValidationException;
import com.marketplace.domain.exception.InsufficientStockException;
import java.util.Objects;

/**
 * Value Object representing the available stock quantity of a product.
 * 
 * Ensures that stock levels never drop below zero. Provides safe methods
 * for adding and subtracting stock quantities.
 */
public class StockQuantity {
    private final int value;

    /**
     * Constructs a StockQuantity instance.
     *
     * @param value The initial stock quantity.
     * @throws DomainValidationException If the value is negative.
     */
    public StockQuantity(int value) {
        if (value < 0) {
            throw new DomainValidationException("Stock quantity cannot be negative");
        }
        this.value = value;
    }

    /**
     * Factory method to create a StockQuantity instance.
     *
     * @param value The initial stock quantity.
     * @return A new StockQuantity instance.
     */
    public static StockQuantity of(int value) {
        return new StockQuantity(value);
    }

    /**
     * Factory method to create a StockQuantity instance representing zero.
     *
     * @return A new StockQuantity instance with zero value.
     */
    public static StockQuantity zero() {
        return new StockQuantity(0);
    }

    /**
     * Adds a specific quantity to the current stock.
     *
     * @param quantity The amount to add.
     * @return A new StockQuantity representing the increased stock.
     * @throws DomainValidationException If the quantity to add is negative.
     */
    public StockQuantity add(int quantity) {
        if (quantity < 0) {
            throw new DomainValidationException("Quantity to add must be positive");
        }
        return new StockQuantity(this.value + quantity);
    }

    /**
     * Subtracts a specific quantity from the current stock.
     *
     * @param quantity The amount to subtract.
     * @return A new StockQuantity representing the decreased stock.
     * @throws DomainValidationException If the quantity to subtract is negative.
     * @throws InsufficientStockException If the quantity to subtract exceeds available stock.
     */
    public StockQuantity subtract(int quantity) {
        if (quantity < 0) {
            throw new DomainValidationException("Quantity to subtract must be positive");
        }
        if (this.value < quantity) {
            throw new InsufficientStockException("Not enough stock available. Current: " + this.value + ", Requested: " + quantity);
        }
        return new StockQuantity(this.value - quantity);
    }

    /**
     * Retrieves the integer value of the stock.
     *
     * @return The stock quantity.
     */
    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockQuantity that = (StockQuantity) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
