package domain.model.valueobject;
import domain.exception.DomainValidationException;
public class Email {
    private final String value;
    public Email(String value) {
        if (value == null || !value.matches("^[A-Za-z0-9+_.-]+@(.+)$")) throw new DomainValidationException("Invalid email format");
        this.value = value;
    }
    public String getValue() { return value; }
}
