package application.domain.services.buyer;

import application.domain.enums.AuditSeverity;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.ChangeBuyerCommercialStatusUseCase;
import application.domain.ports.out.BuyerRepositoryPort;
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
 * Activates or suspends a buyer's commercial status (spec Domain 2). Restricted to
 * {@code ADMIN}. The desired commercial status travels on the supplied {@code target}
 * model.
 */
@Service
@RequiredArgsConstructor
public class ChangeBuyerCommercialStatusService implements ChangeBuyerCommercialStatusUseCase {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public Buyer changeCommercialStatus(User requester, Buyer target) {
        validateUserStatusService.execute(requester);
        validateRoleAuthorizationService.execute(requester, UserRole.ADMIN);

        if (target == null || target.getCommercialStatus() == null) {
            throw new DomainException("Target commercial status is required.");
        }

        Buyer stored = buyerRepositoryPort.findByIdentification(target)
                .orElseThrow(() -> new EntityNotFoundException("Buyer"));

        Map<String, Object> details = new HashMap<>();
        details.put("buyer", stored.auditableId());
        details.put("previousStatus",
                stored.getCommercialStatus() != null ? stored.getCommercialStatus().getCode() : null);
        details.put("newStatus", target.getCommercialStatus().getCode());

        stored.setCommercialStatus(target.getCommercialStatus());
        buyerRepositoryPort.update(stored);

        Operation operation = Operation.of(OperationType.USER_STATUS_CHANGE, requester, stored);
        registerOperationAndAuditService.execute(operation, AuditSeverity.WARNING, details);
        return stored;
    }
}
