package com.marketplace.domain.model.valueobject;

import com.marketplace.domain.exception.DomainValidationException;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object representing a monetary amount.
 * 
 * Ensures that monetary amounts are strictly non-negative and encapsulates
 * safe arithmetic operations using BigDecimal to prevent floating-point inaccuracies.
 * As a Value Object, it is immutable and equality is based on its value.
 */
public class Money {
    private final BigDecimal amount;

    /**
     * Constructs a Money instance.
     *
     * @param amount The monetary value. Cannot be null or negative.
     * @throws DomainValidationException If the amount is null or negative.
     */
    public Money(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("Amount cannot be negative or null");
        }
        this.amount = amount;
    }

    /**
     * Factory method to create a Money instance.
     *
     * @param amount The monetary value.
     * @return A new Money instance.
     */
    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    /**
     * Factory method to create a Money instance with zero value.
     *
     * @return A Money instance representing zero.
     */
    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    /**
     * Adds another Money amount to this instance.
     *
     * @param other The Money amount to add.
     * @return A new Money instance representing the sum.
     */
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    /**
     * Subtracts another Money amount from this instance.
     *
     * @param other The Money amount to subtract.
     * @return A new Money instance representing the difference.
     * @throws DomainValidationException If the subtraction results in a negative amount.
     */
    public Money subtract(Money other) {
        if (this.amount.compareTo(other.amount) < 0) {
            throw new DomainValidationException("Cannot subtract to a negative amount");
        }
        return new Money(this.amount.subtract(other.amount));
    }

    /**
     * Multiplies the current amount by a given integer multiplier.
     *
     * @param multiplier The integer value to multiply by. Cannot be negative.
     * @return A new Money instance representing the product.
     * @throws DomainValidationException If the multiplier is negative.
     */
    public Money multiply(int multiplier) {
        if (multiplier < 0) {
            throw new DomainValidationException("Multiplier cannot be negative");
        }
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)));
    }

    /**
     * Retrieves the raw BigDecimal amount.
     *
     * @return The monetary amount.
     */
    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.doubleValue());
    }

    @Override
    public String toString() {
        return amount.toString();
    }
}
