package domain.model.valueobject;
import domain.exception.DomainValidationException;
public class DocumentId {
    private final String value;
    public DocumentId(String value) {
        if (value == null || value.isBlank()) throw new DomainValidationException("Document ID cannot be empty");
        this.value = value;
    }
    public String getValue() { return value; }
}
