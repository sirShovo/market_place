package application.domain.services.inventory;

import application.domain.enums.AuditSeverity;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.RegisterInventoryReturnUseCase;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.UserRole;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Records stock returned to a warehouse (spec Domain 6), incrementing the item's
 * available stock and appending a {@code RETURN} movement.
 */
@Service
@RequiredArgsConstructor
public class RegisterInventoryReturnService implements RegisterInventoryReturnUseCase {

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public InventoryMovement registerReturn(User requester, InventoryItem probe, int quantity) {
        validateUserStatusService.execute(requester);
        validateRoleAuthorizationService.execute(requester, UserRole.SELLER, UserRole.LOGISTICS_OPERATOR);
        if (quantity <= 0) {
            throw new DomainException("Return quantity must be positive.");
        }

        InventoryItem item = inventoryRepositoryPort.findByProductAndWarehouse(probe)
                .orElseThrow(() -> new EntityNotFoundException("InventoryItem"));

        item.setStock(item.getStock().add(quantity));
        inventoryRepositoryPort.update(item);

        InventoryMovement movement = inventoryRepositoryPort.saveMovement(
                InventoryMovement.of(item, InventoryMovementType.RETURN, quantity, requester));

        Operation operation = Operation.of(OperationType.INVENTORY_RETURN, requester, item);
        registerOperationAndAuditService.execute(operation, AuditSeverity.INFO, Map.of(
                "returned", quantity,
                "resultingStock", item.getStock().value()));
        return movement;
    }
}
