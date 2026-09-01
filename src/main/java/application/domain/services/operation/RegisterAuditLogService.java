package application.domain.services.operation;

import application.domain.models.AuditLog;
import application.domain.ports.out.AuditLogRepositoryPort;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Persists an immutable {@link AuditLog} record, stamping its date when absent.
 */
@Service
@RequiredArgsConstructor
public class RegisterAuditLogService {

    private final AuditLogRepositoryPort auditLogRepositoryPort;

    public AuditLog execute(AuditLog auditLog) {
        if (auditLog.getOperationDate() == null) {
            auditLog.setOperationDate(LocalDateTime.now());
        }
        return auditLogRepositoryPort.save(auditLog);
    }
}
