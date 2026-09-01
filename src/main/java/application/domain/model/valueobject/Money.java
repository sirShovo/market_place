package application.domain.model.valueobject;
import application.domain.exception.DomainValidationException;
import java.math.BigDecimal;

/**
 * Value Object representing a monetary amount.
 * Immutable and ensures no negative amounts.
 */
public class Money {
    private final BigDecimal amount;
    
    /**
     * Constructor for Money.
     * @param amount The monetary amount in BigDecimal.
     */
    public Money(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) throw new DomainValidationException("Amount cannot be null or negative");
        this.amount = amount;
    }
    
    /**
     * Factory method to create Money.
     * @param amount The monetary amount.
     * @return Money instance.
     */
    public static Money of(BigDecimal amount) { return new Money(amount); }
    
    /**
     * Returns a zero Money instance.
     * @return Money instance with zero value.
     */
    public static Money zero() { return new Money(BigDecimal.ZERO); }
    
    /**
     * Adds another Money instance.
     * @param other The other Money instance.
     * @return A new Money instance with the sum.
     */
    public Money add(Money other) { return new Money(this.amount.add(other.amount)); }
    
    /**
     * Subtracts another Money instance.
     * @param other The other Money instance.
     * @return A new Money instance with the difference.
     */
    public Money subtract(Money other) { return new Money(this.amount.subtract(other.amount)); }
    
    /**
     * Multiplies the Money instance by an integer.
     * @param m The multiplier.
     * @return A new Money instance with the product.
     */
    public Money multiply(int m) { return new Money(this.amount.multiply(BigDecimal.valueOf(m))); }
    
    /**
     * Gets the BigDecimal amount.
     * @return The amount.
     */
    public BigDecimal getAmount() { return amount; }
}
