package application.domain.services.inventory;

import application.domain.enums.AuditSeverity;
import application.domain.exceptions.DomainException;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.RegisterInventoryEntryUseCase;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.InventoryItemCondition;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.StockQuantity;
import application.domain.valueobjects.UserRole;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Records incoming stock for a {@code (product, warehouse)} pair, creating the
 * inventory item on first entry, and appends an {@code ENTRY} movement
 * (spec Domain 6).
 */
@Service
@RequiredArgsConstructor
public class RegisterInventoryEntryService implements RegisterInventoryEntryUseCase {

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public InventoryItem registerEntry(User requester, InventoryItem probe, int quantity) {
        validateUserStatusService.execute(requester);
        validateRoleAuthorizationService.execute(requester, UserRole.SELLER, UserRole.LOGISTICS_OPERATOR);
        if (quantity <= 0) {
            throw new DomainException("Entry quantity must be positive.");
        }

        InventoryItem item = inventoryRepositoryPort.findByProductAndWarehouse(probe)
                .orElseGet(() -> {
                    InventoryItem fresh = new InventoryItem();
                    fresh.setProduct(probe.getProduct());
                    fresh.setWarehouse(probe.getWarehouse());
                    fresh.setStock(StockQuantity.of(0));
                    fresh.setCondition(InventoryItemCondition.AVAILABLE);
                    return inventoryRepositoryPort.save(fresh);
                });

        item.setStock(item.getStock().add(quantity));
        inventoryRepositoryPort.update(item);

        inventoryRepositoryPort.saveMovement(
                InventoryMovement.of(item, InventoryMovementType.ENTRY, quantity, requester));

        Operation operation = Operation.of(OperationType.INVENTORY_ENTRY, requester, item);
        registerOperationAndAuditService.execute(operation, AuditSeverity.INFO, Map.of(
                "quantity", quantity,
                "resultingStock", item.getStock().value()));
        return item;
    }
}
