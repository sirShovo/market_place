package application.domain.services.order;

import application.domain.enums.AuditSeverity;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Operation;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.User;
import application.domain.ports.in.DispatchOrderUseCase;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.ProductType;
import application.domain.valueobjects.UserRole;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * A {@code LOGISTICS_OPERATOR} dispatches a paid order that contains physical products
 * ({@code PAID -> DISPATCHED}) and records a {@code SALE_EXIT} inventory movement for
 * each physical line against its fulfillment warehouse (spec Domain 6, Domain 7, §12).
 */
@Service
@RequiredArgsConstructor
public class DispatchOrderService implements DispatchOrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public Order dispatch(User requester, Order order) {
        validateUserStatusService.execute(requester);
        validateRoleAuthorizationService.execute(requester, UserRole.LOGISTICS_OPERATOR);

        Order stored = orderRepositoryPort.findByIdentifier(order)
                .orElseThrow(() -> new EntityNotFoundException("Order"));
        if (!stored.containsPhysicalProducts()) {
            throw new DomainException("Only orders with physical products are dispatched.");
        }

        stored.transitionTo(OrderStatus.DISPATCHED);

        for (OrderItem line : stored.getItems()) {
            if (ProductType.PHYSICAL.equals(line.getProduct().getType())) {
                if (line.getWarehouse() == null) {
                    throw new DomainException(
                            "Physical order item is missing its fulfillment warehouse.");
                }
                InventoryItem probe = new InventoryItem();
                probe.setProduct(line.getProduct());
                probe.setWarehouse(line.getWarehouse());
                InventoryItem inventoryItem = inventoryRepositoryPort
                        .findByProductAndWarehouse(probe)
                        .orElseThrow(() -> new EntityNotFoundException("InventoryItem"));
                inventoryRepositoryPort.saveMovement(InventoryMovement.of(
                        inventoryItem,
                        InventoryMovementType.SALE_EXIT,
                        line.getQuantity(),
                        requester));
            }
        }

        orderRepositoryPort.update(stored);

        Operation operation = Operation.of(OperationType.ORDER_DISPATCH, requester, stored);
        registerOperationAndAuditService.execute(operation, AuditSeverity.INFO, Map.of(
                "order", stored.getIdentifier()));
        return stored;
    }
}
