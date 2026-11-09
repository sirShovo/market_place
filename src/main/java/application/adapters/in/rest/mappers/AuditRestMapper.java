package application.adapters.in.rest.mappers;

import application.adapters.in.rest.responses.AuditLogResponse;
import application.domain.models.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditRestMapper {

    public AuditLogResponse toResponse(AuditLog domain) {
        if (domain == null) {
            return null;
        }
        return new AuditLogResponse(
                domain.getAuditId(),
                domain.getOperationType() != null ? domain.getOperationType().getCode() : null,
                domain.getOperationDate(),
                domain.getPerformedBy() != null ? domain.getPerformedBy().getUsername() : null,
                domain.getUserRole() != null ? domain.getUserRole().getCode() : null,
                domain.getAffectedEntity() != null ? domain.getAffectedEntity().auditableType() : null,
                domain.getAffectedEntity() != null ? domain.getAffectedEntity().auditableId() : null,
                domain.getSeverity() != null ? domain.getSeverity().name() : null,
                domain.getDetails()
        );
    }
}
