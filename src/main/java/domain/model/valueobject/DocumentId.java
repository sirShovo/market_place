package domain.model.valueobject;
import domain.exception.DomainValidationException;

/**
 * Value Object representing a unique document identifier (e.g., ID card).
 */
public class DocumentId {
    private final String value;
    
    /**
     * Constructor for DocumentId.
     * @param value The document ID string.
     */
    public DocumentId(String value) {
        if (value == null || value.isBlank()) throw new DomainValidationException("Document ID cannot be empty");
        this.value = value;
    }
    
    /**
     * Gets the document ID string.
     * @return The document ID value.
     */
    public String getValue() { return value; }
}
