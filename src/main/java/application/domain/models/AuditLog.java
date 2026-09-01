package application.domain.models;

import application.domain.enums.AuditSeverity;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.UserRole;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Immutable, append-only audit record. Persisted in MongoDB; the domain is unaware of
 * that. {@code userRole} is the role held when the action was performed.
 */
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    private String auditId;
    private OperationType operationType;
    private LocalDateTime operationDate;
    private User performedBy;
    private UserRole userRole;
    private AuditableEntity affectedEntity;
    private AuditSeverity severity;
    private Map<String, Object> details;
}
