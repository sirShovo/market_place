package application.domain.services.user;

import application.domain.enums.AuditSeverity;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.ChangeUserStatusUseCase;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.UserRole;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Activates, blocks or deactivates a system user. Restricted to {@code ADMIN}.
 * The desired status is carried on the supplied {@code target} model.
 */
@Service
@RequiredArgsConstructor
public class ChangeUserStatusService implements ChangeUserStatusUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public User changeStatus(User requester, User target) {
        validateUserStatusService.execute(requester);
        validateRoleAuthorizationService.execute(requester, UserRole.ADMIN);

        User stored = userRepositoryPort.findByUsername(target)
                .orElseThrow(() -> new EntityNotFoundException("User"));

        Map<String, Object> details = new HashMap<>();
        details.put("username", stored.getUsername());
        details.put("previousStatus", stored.getStatus() != null ? stored.getStatus().getCode() : null);
        details.put("newStatus", target.getStatus() != null ? target.getStatus().getCode() : null);

        stored.setStatus(target.getStatus());
        userRepositoryPort.update(stored);

        Operation operation = Operation.of(OperationType.USER_STATUS_CHANGE, requester, stored);
        registerOperationAndAuditService.execute(operation, AuditSeverity.WARNING, details);
        return stored;
    }
}
