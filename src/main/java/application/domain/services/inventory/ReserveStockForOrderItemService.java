package application.domain.services.inventory;

import application.domain.enums.AuditSeverity;
import application.domain.exceptions.InvalidReservationException;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Operation;
import application.domain.models.OrderItem;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.InventoryItemCondition;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OperationType;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Reserves stock for a physical {@link OrderItem} at checkout time, choosing among the
 * warehouses that hold {@code AVAILABLE} stock for the product (spec Domain 6, §11).
 *
 * <p>Unlike {@link ReserveInventoryService} — a staff-driven Input Port restricted to
 * {@code SELLER} / {@code LOGISTICS_OPERATOR} — this is an internal collaborator with
 * no Input Port: it runs as a system side effect of the buyer's own checkout, so it
 * performs no role check. {@code performedBy} is recorded as {@code null} on the
 * resulting movement and audit entry, consistent with other buyer-triggered actions
 * such as {@code RegisterBuyerService} and {@code CheckoutCartService}.</p>
 *
 * <p>Reservations are not transactional at this layer: if a later line in the same
 * checkout fails, earlier reservations already made by this service are not rolled
 * back here. Phase 5 wraps the whole checkout in a transaction at the adapter
 * boundary.</p>
 */
@Service
@RequiredArgsConstructor
public class ReserveStockForOrderItemService {

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    /**
     * Reserves {@code item.getQuantity()} units for {@code item.getProduct()} and sets
     * {@code item.warehouse} to the warehouse that fulfilled it.
     *
     * @throws InvalidReservationException if no warehouse holds enough {@code AVAILABLE} stock.
     */
    public InventoryMovement reserve(OrderItem item) {
        List<InventoryItem> candidates = inventoryRepositoryPort.findByProduct(item.getProduct());
        InventoryItem chosen = candidates.stream()
                .filter(candidate -> InventoryItemCondition.AVAILABLE.equals(candidate.getCondition()))
                .filter(candidate -> candidate.getStock().value() >= item.getQuantity())
                .findFirst()
                .orElseThrow(() -> new InvalidReservationException(
                        "No warehouse has enough available stock for product "
                                + item.getProduct().getIdentifier() + "."));

        chosen.setStock(chosen.getStock().subtract(item.getQuantity()));
        inventoryRepositoryPort.update(chosen);
        item.setWarehouse(chosen.getWarehouse());

        InventoryMovement movement = inventoryRepositoryPort.saveMovement(
                InventoryMovement.of(chosen, InventoryMovementType.RESERVATION, item.getQuantity(), null));

        Operation operation = Operation.of(OperationType.INVENTORY_RESERVATION, null, chosen);
        registerOperationAndAuditService.execute(operation, AuditSeverity.INFO, Map.of(
                "product", item.getProduct().getIdentifier(),
                "quantity", item.getQuantity(),
                "warehouse", chosen.getWarehouse().getIdentifier()));
        return movement;
    }
}
