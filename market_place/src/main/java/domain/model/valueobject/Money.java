package domain.model.valueobject;
import domain.exception.DomainValidationException;
import java.math.BigDecimal;
public class Money {
    private final BigDecimal amount;
    public Money(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) throw new DomainValidationException("Amount cannot be null or negative");
        this.amount = amount;
    }
    public static Money of(BigDecimal amount) { return new Money(amount); }
    public static Money zero() { return new Money(BigDecimal.ZERO); }
    public Money add(Money other) { return new Money(this.amount.add(other.amount)); }
    public Money subtract(Money other) { return new Money(this.amount.subtract(other.amount)); }
    public Money multiply(int m) { return new Money(this.amount.multiply(BigDecimal.valueOf(m))); }
    public BigDecimal getAmount() { return amount; }
}
