package application.adapters.out.persistence.mongodb.mappers;

import application.adapters.out.persistence.mongodb.documents.AuditLogDocument;
import application.domain.enums.AuditSeverity;
import application.domain.models.AuditLog;
import application.domain.models.AuditableEntity;
import application.domain.models.User;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.UserRole;
import java.util.HashMap;
import org.springframework.stereotype.Component;

@Component
public class AuditLogDocumentMapper {

    public AuditLogDocument toDocument(AuditLog domain) {
        if (domain == null) {
            return null;
        }
        AuditLogDocument doc = new AuditLogDocument();
        doc.setAuditId(domain.getAuditId());
        doc.setOperationType(domain.getOperationType() != null ? domain.getOperationType().getCode() : null);
        doc.setOperationDate(domain.getOperationDate());
        if (domain.getPerformedBy() != null) {
            doc.setPerformedByUserId(domain.getPerformedBy().getUserId());
            doc.setPerformedByUsername(domain.getPerformedBy().getUsername());
            if (domain.getPerformedBy().getIdentification() != null) {
                doc.setPerformedByIdentification(domain.getPerformedBy().getIdentification().value());
            }
        }
        doc.setUserRole(domain.getUserRole() != null ? domain.getUserRole().getCode() : null);
        if (domain.getAffectedEntity() != null) {
            doc.setAffectedEntityType(domain.getAffectedEntity().auditableType());
            doc.setAffectedEntityId(domain.getAffectedEntity().auditableId());
        }
        doc.setSeverity(domain.getSeverity() != null ? domain.getSeverity().name() : AuditSeverity.INFO.name());
        doc.setDetails(domain.getDetails() != null ? new HashMap<>(domain.getDetails()) : new HashMap<>());
        return doc;
    }

    public AuditLog toDomain(AuditLogDocument doc) {
        if (doc == null) {
            return null;
        }
        AuditLog domain = new AuditLog();
        domain.setAuditId(doc.getAuditId());
        domain.setOperationType(OperationType.fromCode(doc.getOperationType()));
        domain.setOperationDate(doc.getOperationDate());

        if (doc.getPerformedByUsername() != null || doc.getPerformedByUserId() != null) {
            User user = new User();
            user.setUserId(doc.getPerformedByUserId());
            user.setUsername(doc.getPerformedByUsername());
            if (doc.getPerformedByIdentification() != null && !doc.getPerformedByIdentification().isBlank()) {
                user.setIdentification(new DocumentId(doc.getPerformedByIdentification()));
            }
            user.setRole(UserRole.fromCode(doc.getUserRole()));
            domain.setPerformedBy(user);
        }

        domain.setUserRole(UserRole.fromCode(doc.getUserRole()));
        domain.setAffectedEntity(
                AuditableEntity.of(doc.getAffectedEntityType(), doc.getAffectedEntityId()));
        if (doc.getSeverity() != null) {
            domain.setSeverity(AuditSeverity.valueOf(doc.getSeverity()));
        }
        domain.setDetails(doc.getDetails() != null ? new HashMap<>(doc.getDetails()) : new HashMap<>());
        return domain;
    }
}
