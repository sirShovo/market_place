package domain.model.valueobject;
import domain.exception.DomainValidationException;

/**
 * Value Object representing an email address.
 * Ensures the email is properly formatted.
 */
public class Email {
    private final String value;
    
    /**
     * Constructor for Email.
     * @param value The email string.
     */
    public Email(String value) {
        if (value == null || !value.matches("^[A-Za-z0-9+_.-]+@(.+)$")) throw new DomainValidationException("Invalid email format");
        this.value = value;
    }
    
    /**
     * Gets the email string.
     * @return The email value.
     */
    public String getValue() { return value; }
}
