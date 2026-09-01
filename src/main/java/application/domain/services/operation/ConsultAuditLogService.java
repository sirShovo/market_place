package application.domain.services.operation;

import application.domain.models.AuditLog;
import application.domain.models.AuditableEntity;
import application.domain.models.User;
import application.domain.ports.in.ConsultAuditLogUseCase;
import application.domain.ports.out.AuditLogRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.valueobjects.UserRole;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Reads the audit history for an entity. Restricted to {@code ADMIN} / {@code SUPERVISOR}.
 */
@Service
@RequiredArgsConstructor
public class ConsultAuditLogService implements ConsultAuditLogUseCase {

    private final AuditLogRepositoryPort auditLogRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;

    @Override
    public List<AuditLog> consult(User requester, AuditableEntity entity) {
        validateUserStatusService.execute(requester);
        validateRoleAuthorizationService.execute(requester, UserRole.ADMIN, UserRole.SUPERVISOR);
        return auditLogRepositoryPort.findByEntity(entity);
    }
}
