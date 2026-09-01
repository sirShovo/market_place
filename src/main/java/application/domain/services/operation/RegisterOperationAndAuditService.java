package application.domain.services.operation;

import application.domain.enums.AuditSeverity;
import application.domain.models.AuditLog;
import application.domain.models.Operation;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Records a significant business action: persists the {@link Operation} and a matching
 * {@link AuditLog} record. Every command service composes this collaborator
 * (spec traceability requirements).
 */
@Service
@RequiredArgsConstructor
public class RegisterOperationAndAuditService {

    private final RegisterOperationService registerOperationService;
    private final RegisterAuditLogService registerAuditLogService;

    public void execute(Operation operation, AuditSeverity severity, Map<String, Object> details) {
        Operation saved = registerOperationService.execute(operation);

        AuditLog auditLog = new AuditLog();
        auditLog.setOperationType(saved.getOperationType());
        auditLog.setOperationDate(LocalDateTime.now());
        auditLog.setPerformedBy(saved.getPerformedBy());
        auditLog.setUserRole(saved.getPerformedBy() != null ? saved.getPerformedBy().getRole() : null);
        auditLog.setAffectedEntity(saved.getAffectedEntity());
        auditLog.setSeverity(severity);
        auditLog.setDetails(details);

        registerAuditLogService.execute(auditLog);
    }
}
