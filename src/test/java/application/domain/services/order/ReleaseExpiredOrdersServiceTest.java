package application.domain.services.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.PhysicalProduct;
import application.domain.models.Product;
import application.domain.models.Warehouse;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.support.Fakes;
import application.domain.support.Fakes.AuditStore;
import application.domain.support.Fakes.OperationStore;
import application.domain.valueobjects.InventoryItemCondition;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.StockQuantity;
import application.domain.valueobjects.WarehouseType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReleaseExpiredOrdersServiceTest {

    private final List<Order> orders = new ArrayList<>();
    private final List<InventoryItem> inventoryItems = new ArrayList<>();
    private final List<InventoryMovement> movements = new ArrayList<>();
    private final OperationStore ops = new OperationStore();
    private final AuditStore audits = new AuditStore();
    private ReleaseExpiredOrdersService service;

    @BeforeEach
    void setUp() {
        OrderRepositoryPort orderRepo = new OrderRepositoryPort() {
            @Override
            public Order save(Order order) {
                orders.add(order);
                return order;
            }

            @Override
            public Optional<Order> findByIdentifier(Order order) {
                return orders.stream()
                        .filter(o -> o.getIdentifier().equals(order.getIdentifier()))
                        .findFirst();
            }

            @Override
            public List<Order> findByBuyer(application.domain.models.Buyer buyer) {
                return orders;
            }

            @Override
            public List<Order> findByStatus(OrderStatus status) {
                return orders.stream().filter(o -> status.equals(o.getStatus())).toList();
            }

            @Override
            public void update(Order order) {
            }
        };

        InventoryRepositoryPort invRepo = new InventoryRepositoryPort() {
            @Override
            public InventoryItem save(InventoryItem item) {
                inventoryItems.add(item);
                return item;
            }

            @Override
            public Optional<InventoryItem> findByProductAndWarehouse(InventoryItem probe) {
                return inventoryItems.stream()
                        .filter(i -> i.getProduct().getIdentifier().equals(probe.getProduct().getIdentifier())
                                && i.getWarehouse().getIdentifier().equals(probe.getWarehouse().getIdentifier()))
                        .findFirst();
            }

            @Override
            public List<InventoryItem> findByProduct(Product product) {
                return inventoryItems;
            }

            @Override
            public void update(InventoryItem item) {
            }

            @Override
            public InventoryMovement saveMovement(InventoryMovement movement) {
                movements.add(movement);
                return movement;
            }

            @Override
            public List<InventoryMovement> findMovements(InventoryItem item) {
                return movements;
            }
        };

        service = new ReleaseExpiredOrdersService(
                orderRepo,
                invRepo,
                () -> 30,
                Fakes.auditService(ops, audits));
    }

    @Test
    void releasesReservedStockForExpiredPendingPaymentOrdersIdempotently() {
        PhysicalProduct product = new PhysicalProduct();
        product.setIdentifier("PROD-1");
        product.setName("Monitor");

        Warehouse warehouse = new Warehouse();
        warehouse.setIdentifier("WH-1");
        warehouse.setName("Central");
        warehouse.setType(WarehouseType.MARKETPLACE);

        InventoryItem item = new InventoryItem();
        item.setId(1L);
        item.setProduct(product);
        item.setWarehouse(warehouse);
        item.setStock(StockQuantity.of(7));
        item.setCondition(InventoryItemCondition.AVAILABLE);
        inventoryItems.add(item);

        OrderItem expiredLine = new OrderItem();
        expiredLine.setProduct(product);
        expiredLine.setQuantity(3);
        expiredLine.setUnitPrice(Money.of(BigDecimal.valueOf(200)));
        expiredLine.setWarehouse(warehouse);

        Order expiredOrder = new Order();
        expiredOrder.setIdentifier("ORD-EXPIRED");
        expiredOrder.addItem(expiredLine);
        expiredOrder.transitionTo(OrderStatus.PENDING_PAYMENT);
        expiredOrder.setCreatedAt(LocalDateTime.now().minusMinutes(45));
        orders.add(expiredOrder);

        Order freshOrder = new Order();
        freshOrder.setIdentifier("ORD-FRESH");
        OrderItem freshLine = new OrderItem();
        freshLine.setProduct(product);
        freshLine.setQuantity(2);
        freshLine.setUnitPrice(Money.of(BigDecimal.valueOf(200)));
        freshLine.setWarehouse(warehouse);
        freshOrder.addItem(freshLine);
        freshOrder.transitionTo(OrderStatus.PENDING_PAYMENT);
        freshOrder.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        orders.add(freshOrder);

        int releasedFirstRun = service.releaseExpiredOrders();
        assertEquals(1, releasedFirstRun);
        assertEquals(10, item.getStock().value());
        assertNull(expiredLine.getWarehouse());
        assertEquals(1, movements.size());
        assertEquals(1, ops.saved.size());
        assertEquals(1, audits.saved.size());

        int releasedSecondRun = service.releaseExpiredOrders();
        assertEquals(0, releasedSecondRun);
        assertEquals(10, item.getStock().value());
    }
}
