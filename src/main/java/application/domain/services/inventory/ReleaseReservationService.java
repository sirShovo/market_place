package application.domain.services.inventory;

import application.domain.enums.AuditSeverity;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.ReleaseReservationUseCase;
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
 * Returns previously reserved stock to available inventory (e.g. an unpaid order that
 * expired). Recorded as an {@code ADJUSTMENT} movement with an
 * {@code INVENTORY_RELEASE} operation.
 */
@Service
@RequiredArgsConstructor
public class ReleaseReservationService implements ReleaseReservationUseCase {

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public InventoryMovement release(User requester, InventoryItem probe, int quantity) {
        validateUserStatusService.execute(requester);
        validateRoleAuthorizationService.execute(requester, UserRole.SELLER, UserRole.LOGISTICS_OPERATOR);
        if (quantity <= 0) {
            throw new DomainException("Release quantity must be positive.");
        }

        InventoryItem item = inventoryRepositoryPort.findByProductAndWarehouse(probe)
                .orElseThrow(() -> new EntityNotFoundException("InventoryItem"));

        item.setStock(item.getStock().add(quantity));
        inventoryRepositoryPort.update(item);

        InventoryMovement movement = inventoryRepositoryPort.saveMovement(
                InventoryMovement.of(item, InventoryMovementType.ADJUSTMENT, quantity, requester));

        Operation operation = Operation.of(OperationType.INVENTORY_RELEASE, requester, item);
        registerOperationAndAuditService.execute(operation, AuditSeverity.INFO, Map.of(
                "released", quantity,
                "resultingStock", item.getStock().value()));
        return movement;
    }
}
