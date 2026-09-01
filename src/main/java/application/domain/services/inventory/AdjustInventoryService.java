package application.domain.services.inventory;

import application.domain.enums.AuditSeverity;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.AdjustInventoryUseCase;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.StockQuantity;
import application.domain.valueobjects.UserRole;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Manually corrects the stock of an inventory item to a new absolute quantity
 * (spec Domain 6). Appends an {@code ADJUSTMENT} movement recording the delta.
 */
@Service
@RequiredArgsConstructor
public class AdjustInventoryService implements AdjustInventoryUseCase {

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public InventoryMovement adjust(User requester, InventoryItem probe, int newQuantity) {
        validateUserStatusService.execute(requester);
        validateRoleAuthorizationService.execute(requester, UserRole.SELLER, UserRole.LOGISTICS_OPERATOR);

        InventoryItem item = inventoryRepositoryPort.findByProductAndWarehouse(probe)
                .orElseThrow(() -> new EntityNotFoundException("InventoryItem"));

        int previous = item.getStock().value();
        int delta = Math.abs(newQuantity - previous);
        item.setStock(StockQuantity.of(newQuantity));
        inventoryRepositoryPort.update(item);

        InventoryMovement movement = inventoryRepositoryPort.saveMovement(
                InventoryMovement.of(item, InventoryMovementType.ADJUSTMENT, delta, requester));

        Map<String, Object> details = new HashMap<>();
        details.put("previousStock", previous);
        details.put("newStock", newQuantity);
        Operation operation = Operation.of(OperationType.INVENTORY_ADJUSTMENT, requester, item);
        registerOperationAndAuditService.execute(operation, AuditSeverity.WARNING, details);
        return movement;
    }
}
