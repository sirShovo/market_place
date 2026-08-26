package domain.model.entity;
import domain.model.valueobject.*;
public class User {
    private Long id;
    private String fullName;
    private Email email;
    private DocumentId documentId;
    private UserRole role;
    private UserStatus status;
    public User(String fullName, Email email, DocumentId documentId, UserRole role) {
        this.fullName = fullName; this.email = email; this.documentId = documentId; this.role = role; this.status = UserStatus.ACTIVE;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public Email getEmail() { return email; }
    public DocumentId getDocumentId() { return documentId; }
    public UserRole getRole() { return role; }
    public UserStatus getStatus() { return status; }
}
