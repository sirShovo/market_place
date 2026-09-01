package application.domain.services.inventory;

import application.domain.enums.AuditSeverity;
import application.domain.exceptions.InvalidReservationException;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.ReserveInventoryUseCase;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.InventoryItemCondition;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.UserRole;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Reserves stock for an order. Rejects a reservation against non-existent or
 * {@code DAMAGED} inventory, or any move that would drive stock negative
 * (spec Domain 6, §11). Appends a {@code RESERVATION} movement.
 */
@Service
@RequiredArgsConstructor
public class ReserveInventoryService implements ReserveInventoryUseCase {

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public InventoryMovement reserve(User requester, InventoryItem probe, int quantity) {
        validateUserStatusService.execute(requester);
        validateRoleAuthorizationService.execute(requester, UserRole.SELLER, UserRole.LOGISTICS_OPERATOR);
        if (quantity <= 0) {
            throw new InvalidReservationException("Reservation quantity must be positive.");
        }

        InventoryItem item = inventoryRepositoryPort.findByProductAndWarehouse(probe)
                .orElseThrow(() -> new InvalidReservationException("Inventory does not exist for this product and warehouse."));
        if (InventoryItemCondition.DAMAGED.equals(item.getCondition())) {
            throw new InvalidReservationException("Damaged inventory cannot be reserved.");
        }

        item.setStock(item.getStock().subtract(quantity));
        inventoryRepositoryPort.update(item);

        InventoryMovement movement = inventoryRepositoryPort.saveMovement(
                InventoryMovement.of(item, InventoryMovementType.RESERVATION, quantity, requester));

        Operation operation = Operation.of(OperationType.INVENTORY_RESERVATION, requester, item);
        registerOperationAndAuditService.execute(operation, AuditSeverity.INFO, Map.of(
                "quantity", quantity,
                "remainingStock", item.getStock().value()));
        return movement;
    }
}
