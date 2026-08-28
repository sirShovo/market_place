package domain.model.entity;
import domain.model.valueobject.*;

/**
 * Domain entity representing a Market Place user.
 */
public class User {
    private Long id;
    private String fullName;
    private Email email;
    private DocumentId documentId;
    private UserRole role;
    private UserStatus status;
    
    /**
     * Constructor for User.
     * @param fullName The user's full name.
     * @param email The user's validated email address.
     * @param documentId The user's validated document ID.
     * @param role The user's role in the platform.
     */
    public User(String fullName, Email email, DocumentId documentId, UserRole role) {
        this.fullName = fullName; this.email = email; this.documentId = documentId; this.role = role; this.status = UserStatus.ACTIVE;
    }
    
    /** @return The User ID */
    public Long getId() { return id; }
    /** @param id The User ID to set */
    public void setId(Long id) { this.id = id; }
    
    /** @return The User's full name */
    public String getFullName() { return fullName; }
    
    /** @return The User's email */
    public Email getEmail() { return email; }
    
    /** @return The User's document ID */
    public DocumentId getDocumentId() { return documentId; }
    
    /** @return The User's role */
    public UserRole getRole() { return role; }
    
    /** @return The User's status */
    public UserStatus getStatus() { return status; }
}
