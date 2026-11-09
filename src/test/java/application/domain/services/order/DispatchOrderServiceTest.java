package application.domain.services.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.DigitalProduct;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.PhysicalProduct;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.support.Fakes;
import application.domain.valueobjects.InventoryItemCondition;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.StockQuantity;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.UserStatus;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DispatchOrderServiceTest {

    private FakeOrderRepo orderRepo;
    private FakeInventoryRepo inventoryRepo;
    private Fakes.OperationStore ops;
    private DispatchOrderService service;

    @BeforeEach
    void setUp() {
        orderRepo = new FakeOrderRepo();
        inventoryRepo = new FakeInventoryRepo();
        ops = new Fakes.OperationStore();
        service = new DispatchOrderService(
                orderRepo,
                inventoryRepo,
                new ValidateUserStatusService(),
                new ValidateRoleAuthorizationService(),
                Fakes.auditService(ops, new Fakes.AuditStore()));
    }

    private User operator() {
        User u = new User();
        u.setRole(UserRole.LOGISTICS_OPERATOR);
        u.setStatus(UserStatus.ACTIVE);
        return u;
    }

    private Order paidPhysicalOrder(Warehouse warehouse) {
        PhysicalProduct p = new PhysicalProduct();
        p.setIdentifier("SKU-PHYS-1");
        p.setPrice(Money.of(new BigDecimal("40.00")));

        OrderItem line = new OrderItem();
        line.setProduct(p);
        line.setQuantity(2);
        line.setUnitPrice(p.getPrice());
        line.setWarehouse(warehouse);

        Order o = new Order();
        o.setIdentifier("ORD-100");
        o.addItem(line);
        o.transitionTo(OrderStatus.PENDING_PAYMENT);
        o.transitionTo(OrderStatus.PAID);
        return o;
    }

    @Test
    void dispatchesPaidPhysicalOrderAndLogsSaleExitMovement() {
        Warehouse wh = new Warehouse();
        wh.setIdentifier("WH-1");
        Order order = paidPhysicalOrder(wh);
        orderRepo.stored = order;

        InventoryItem inv = new InventoryItem();
        inv.setProduct(order.getItems().get(0).getProduct());
        inv.setWarehouse(wh);
        inv.setStock(StockQuantity.of(8));
        inv.setCondition(InventoryItemCondition.AVAILABLE);
        inventoryRepo.stored = inv;

        Order dispatched = service.dispatch(operator(), order);
        assertEquals(OrderStatus.DISPATCHED, dispatched.getStatus());
        assertEquals(1, inventoryRepo.movements.size());
        assertEquals(InventoryMovementType.SALE_EXIT, inventoryRepo.movements.get(0).getType());
        assertEquals(2, inventoryRepo.movements.get(0).getQuantity());
        assertEquals(1, ops.saved.size());
        assertEquals(OperationType.ORDER_DISPATCH, ops.saved.get(0).getOperationType());
    }

    @Test
    void rejectsDispatchWhenPhysicalLineHasNoWarehouse() {
        orderRepo.stored = paidPhysicalOrder(null);
        assertThrows(DomainException.class,
                () -> service.dispatch(operator(), orderRepo.stored));
    }

    @Test
    void rejectsDispatchOfDigitalOnlyOrder() {
        DigitalProduct d = new DigitalProduct();
        d.setIdentifier("SKU-DIG-1");
        d.setPrice(Money.of(new BigDecimal("15.00")));

        OrderItem line = new OrderItem();
        line.setProduct(d);
        line.setQuantity(1);
        line.setUnitPrice(d.getPrice());

        Order o = new Order();
        o.setIdentifier("ORD-DIG");
        o.addItem(line);
        o.transitionTo(OrderStatus.PENDING_PAYMENT);
        o.transitionTo(OrderStatus.PAID);
        orderRepo.stored = o;

        assertThrows(DomainException.class,
                () -> service.dispatch(operator(), o));
    }

    @Test
    void rejectsNonLogisticsOperator() {
        Warehouse wh = new Warehouse();
        wh.setIdentifier("WH-1");
        orderRepo.stored = paidPhysicalOrder(wh);

        User admin = operator();
        admin.setRole(UserRole.ADMIN);
        assertThrows(UnauthorizedOperationException.class,
                () -> service.dispatch(admin, orderRepo.stored));
    }

    private static final class FakeOrderRepo implements OrderRepositoryPort {
        Order stored;

        @Override
        public Order save(Order order) {
            stored = order;
            return order;
        }

        @Override
        public Optional<Order> findByIdentifier(Order order) {
            return Optional.ofNullable(stored);
        }

        @Override
        public List<Order> findByBuyer(application.domain.models.Buyer buyer) {
            return stored == null ? List.of() : List.of(stored);
        }

        @Override
        public void update(Order order) {
            stored = order;
        }
    }

    private static final class FakeInventoryRepo implements InventoryRepositoryPort {
        InventoryItem stored;
        final List<InventoryMovement> movements = new ArrayList<>();

        @Override
        public InventoryItem save(InventoryItem item) {
            stored = item;
            return item;
        }

        @Override
        public Optional<InventoryItem> findByProductAndWarehouse(InventoryItem probe) {
            return Optional.ofNullable(stored);
        }

        @Override
        public List<InventoryItem> findByProduct(Product product) {
            return stored == null ? List.of() : List.of(stored);
        }

        @Override
        public void update(InventoryItem item) {
            stored = item;
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
    }
}
