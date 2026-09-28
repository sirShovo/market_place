package application.domain.services.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.InvalidReservationException;
import application.domain.models.Buyer;
import application.domain.models.Cart;
import application.domain.models.CartItem;
import application.domain.models.DigitalProduct;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.models.PhysicalProduct;
import application.domain.models.Product;
import application.domain.models.Warehouse;
import application.domain.ports.out.CartRepositoryPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.authorization.ValidateBuyerCanPurchaseService;
import application.domain.services.inventory.ReserveStockForOrderItemService;
import application.domain.support.Fakes;
import application.domain.valueobjects.BuyerCommercialStatus;
import application.domain.valueobjects.InventoryItemCondition;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.StockQuantity;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CheckoutCartServiceTest {

    private FakeCartRepo cartRepo;
    private FakeOrderRepo orderRepo;
    private FakeInventoryRepo inventoryRepo;
    private CheckoutCartService service;

    @BeforeEach
    void setUp() {
        cartRepo = new FakeCartRepo();
        orderRepo = new FakeOrderRepo();
        inventoryRepo = new FakeInventoryRepo();
        ReserveStockForOrderItemService reserveStockForOrderItemService = new ReserveStockForOrderItemService(
                inventoryRepo, Fakes.auditService(new Fakes.OperationStore(), new Fakes.AuditStore()));
        service = new CheckoutCartService(
                cartRepo,
                orderRepo,
                new ValidateBuyerCanPurchaseService(),
                reserveStockForOrderItemService,
                Fakes.auditService(new Fakes.OperationStore(), new Fakes.AuditStore()));
    }

    private Buyer buyer() {
        Buyer buyer = new Buyer();
        buyer.setCommercialStatus(BuyerCommercialStatus.ACTIVE);
        return buyer;
    }

    private Warehouse warehouse(String id) {
        Warehouse warehouse = new Warehouse();
        warehouse.setIdentifier(id);
        return warehouse;
    }

    private InventoryItem stockOf(Product product, Warehouse warehouse, int quantity) {
        InventoryItem item = new InventoryItem();
        item.setProduct(product);
        item.setWarehouse(warehouse);
        item.setStock(StockQuantity.of(quantity));
        item.setCondition(InventoryItemCondition.AVAILABLE);
        return item;
    }

    private CartItem cartItemOf(Product product, int quantity) {
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }

    @Test
    void checkoutReservesStockForPhysicalLinesAndSetsTheirWarehouse() {
        PhysicalProduct product = new PhysicalProduct();
        product.setIdentifier("P-1");
        product.setPrice(Money.of(new BigDecimal("20.00")));
        Warehouse warehouse = warehouse("W-1");
        inventoryRepo.items.add(stockOf(product, warehouse, 5));

        Cart cart = new Cart();
        cart.setBuyer(buyer());
        cart.getItems().add(cartItemOf(product, 2));
        cartRepo.active = cart;

        Order order = service.checkout(cart.getBuyer());

        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());
        assertEquals(warehouse, order.getItems().get(0).getWarehouse());
        assertEquals(3, inventoryRepo.items.get(0).getStock().value());
        assertEquals(1, inventoryRepo.movements.size());
    }

    @Test
    void checkoutFailsWhenNoWarehouseHasEnoughStock() {
        PhysicalProduct product = new PhysicalProduct();
        product.setIdentifier("P-1");
        product.setPrice(Money.of(new BigDecimal("20.00")));
        inventoryRepo.items.add(stockOf(product, warehouse("W-1"), 1));

        Cart cart = new Cart();
        cart.setBuyer(buyer());
        cart.getItems().add(cartItemOf(product, 2));
        cartRepo.active = cart;

        assertThrows(InvalidReservationException.class, () -> service.checkout(cart.getBuyer()));
    }

    @Test
    void digitalLinesNeedNoReservation() {
        DigitalProduct product = new DigitalProduct();
        product.setIdentifier("P-2");
        product.setPrice(Money.of(new BigDecimal("15.00")));

        Cart cart = new Cart();
        cart.setBuyer(buyer());
        cart.getItems().add(cartItemOf(product, 1));
        cartRepo.active = cart;

        Order order = service.checkout(cart.getBuyer());

        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());
        assertNull(order.getItems().get(0).getWarehouse());
        assertEquals(0, inventoryRepo.movements.size());
    }

    @Test
    void emptyCartCannotBeCheckedOut() {
        Cart cart = new Cart();
        cart.setBuyer(buyer());
        cartRepo.active = cart;

        assertThrows(DomainException.class, () -> service.checkout(cart.getBuyer()));
    }

    private static final class FakeCartRepo implements CartRepositoryPort {
        Cart active;

        @Override
        public Cart save(Cart cart) {
            active = cart;
            return cart;
        }

        @Override
        public Optional<Cart> findActiveByBuyer(Buyer buyer) {
            return Optional.ofNullable(active);
        }

        @Override
        public Optional<Cart> findById(Cart cart) {
            return Optional.ofNullable(active);
        }

        @Override
        public void update(Cart cart) {
            active = cart;
        }
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
        public List<Order> findByBuyer(Buyer buyer) {
            return stored == null ? List.of() : List.of(stored);
        }

        @Override
        public void update(Order order) {
            stored = order;
        }
    }

    private static final class FakeInventoryRepo implements InventoryRepositoryPort {
        final List<InventoryItem> items = new ArrayList<>();
        final List<InventoryMovement> movements = new ArrayList<>();

        @Override
        public InventoryItem save(InventoryItem item) {
            items.add(item);
            return item;
        }

        @Override
        public Optional<InventoryItem> findByProductAndWarehouse(InventoryItem probe) {
            return items.stream()
                    .filter(i -> i.getProduct() == probe.getProduct() && i.getWarehouse() == probe.getWarehouse())
                    .findFirst();
        }

        @Override
        public List<InventoryItem> findByProduct(Product product) {
            return items.stream().filter(i -> i.getProduct() == product).toList();
        }

        @Override
        public void update(InventoryItem item) {
            // items already holds the same mutable reference; nothing to replace.
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
