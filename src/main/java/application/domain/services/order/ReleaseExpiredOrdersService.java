package application.domain.services.order;

import application.domain.enums.AuditSeverity;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Operation;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.ports.in.ReleaseExpiredOrdersUseCase;
import application.domain.ports.out.BusinessConfigurationPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.ProductType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Scans {@code PENDING_PAYMENT} orders that have exceeded the unpaid order expiration
 * window configured via {@link BusinessConfigurationPort} and restores their reserved
 * physical stock back to available inventory.
 */
@Service
@RequiredArgsConstructor
public class ReleaseExpiredOrdersService implements ReleaseExpiredOrdersUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final BusinessConfigurationPort businessConfigurationPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public int releaseExpiredOrders() {
        int expirationMinutes = businessConfigurationPort.getUnpaidOrderExpirationMinutes();
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(expirationMinutes);

        List<Order> pendingOrders = orderRepositoryPort.findByStatus(OrderStatus.PENDING_PAYMENT);
        int releasedOrdersCount = 0;

        for (Order order : pendingOrders) {
            if (order == null || order.getCreatedAt() == null || !order.getCreatedAt().isBefore(cutoff)) {
                continue;
            }
            boolean releasedAnyLine = false;
            if (order.getItems() != null) {
                for (OrderItem line : order.getItems()) {
                    if (line != null
                            && line.getProduct() != null
                            && ProductType.PHYSICAL.equals(line.getProduct().getType())
                            && line.getWarehouse() != null
                            && line.getQuantity() > 0) {

                        InventoryItem probe = new InventoryItem();
                        probe.setProduct(line.getProduct());
                        probe.setWarehouse(line.getWarehouse());

                        inventoryRepositoryPort.findByProductAndWarehouse(probe).ifPresent(item -> {
                            item.setStock(item.getStock().add(line.getQuantity()));
                            inventoryRepositoryPort.update(item);

                            inventoryRepositoryPort.saveMovement(InventoryMovement.of(
                                    item,
                                    InventoryMovementType.ADJUSTMENT,
                                    line.getQuantity(),
                                    null));

                            Operation operation = Operation.of(
                                    OperationType.INVENTORY_RELEASE, null, item);
                            registerOperationAndAuditService.execute(
                                    operation,
                                    AuditSeverity.INFO,
                                    Map.of(
                                            "orderIdentifier", order.getIdentifier(),
                                            "productIdentifier", line.getProduct().getIdentifier(),
                                            "warehouseIdentifier", line.getWarehouse().getIdentifier(),
                                            "releasedQuantity", line.getQuantity(),
                                            "resultingStock", item.getStock().value(),
                                            "reason", "UNPAID_ORDER_EXPIRED"
                                    ));
                        });

                        line.setWarehouse(null);
                        releasedAnyLine = true;
                    }
                }
            }
            if (releasedAnyLine) {
                orderRepositoryPort.update(order);
                releasedOrdersCount++;
            }
        }
        return releasedOrdersCount;
    }
}
