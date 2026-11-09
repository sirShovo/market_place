package application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.enums.PaymentResult;
import application.domain.exceptions.FinalizedOrderModificationException;
import application.domain.models.AuditLog;
import application.domain.models.Buyer;
import application.domain.models.CartItem;
import application.domain.models.Category;
import application.domain.models.DigitalProduct;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.models.PhysicalProduct;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.CartRepositoryPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.authorization.ValidateBuyerCanPurchaseService;
import application.domain.services.authorization.ValidateBuyerOwnsOrderService;
import application.domain.services.authorization.ValidateOrderAccessService;
import application.domain.services.authorization.ValidateProductOwnershipService;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.services.buyer.RegisterBuyerService;
import application.domain.services.cart.AddCartItemService;
import application.domain.services.catalog.ConsultCatalogService;
import application.domain.services.catalog.PublishProductService;
import application.domain.services.identity.ValidatePlatformUniquenessService;
import application.domain.services.inventory.RegisterInventoryEntryService;
import application.domain.services.inventory.ReserveStockForOrderItemService;
import application.domain.services.operation.ConsultAuditLogService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.services.order.CheckoutCartService;
import application.domain.services.order.ConfirmDeliveryService;
import application.domain.services.order.DispatchOrderService;
import application.domain.services.order.ProcessOrderPaymentService;
import application.domain.services.seller.OnboardSellerService;
import application.domain.services.user.LoginService;
import application.domain.services.user.RegisterUserService;
import application.domain.support.Fakes;
import application.domain.support.Fakes.AuditStore;
import application.domain.support.Fakes.OperationStore;
import application.domain.valueobjects.CartStatus;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.InventoryItemCondition;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.UserRole;
import application.infrastructure.config.DataSeeder;
import application.infrastructure.security.BCryptPasswordAdapter;
import application.infrastructure.security.JjwtAdapter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

/**
 * End-to-end integration test exercising the complete NexusMarket business workflow
 * across all participant roles: Admin bootstrap, Seller onboarding, Logistics Operator
 * registration, Catalog publication, Inventory entry, Buyer self-registration, Cart
 * management, Checkout with stock reservation, Payment processing, Order dispatch with
 * SALE_EXIT movement tracking, Delivery confirmation, Finalized order immutability, and
 * Audit trail consultation.
 */
class FullMarketplaceFlowIntegrationTest {

    @Test
    void fullMultiRoleMarketplaceLifecycleExecutesEndToEnd() {
        List<User> users = new ArrayList<>();
        List<Buyer> buyers = new ArrayList<>();
        List<Seller> sellers = new ArrayList<>();
        List<Warehouse> warehouses = new ArrayList<>();
        List<Product> products = new ArrayList<>();
        List<InventoryItem> inventoryItems = new ArrayList<>();
        List<InventoryMovement> movements = new ArrayList<>();
        List<application.domain.models.Cart> carts = new ArrayList<>();
        List<Order> orders = new ArrayList<>();
        AtomicLong idSeq = new AtomicLong(1);

        UserRepositoryPort userRepo = new UserRepositoryPort() {
            @Override
            public User save(User user) {
                if (user.getUserId() == null) {
                    user.setUserId((int) idSeq.getAndIncrement());
                }
                users.removeIf(u -> u.getUsername().equals(user.getUsername()));
                users.add(user);
                return user;
            }

            @Override
            public Optional<User> findByUsername(User user) {
                return users.stream().filter(u -> u.getUsername().equals(user.getUsername())).findFirst();
            }

            @Override
            public Optional<User> findByIdentification(User user) {
                return users.stream()
                        .filter(u -> u.getIdentification().equals(user.getIdentification()))
                        .findFirst();
            }

            @Override
            public boolean existsByIdentification(DocumentId identification) {
                return users.stream().anyMatch(u -> identification.equals(u.getIdentification()));
            }

            @Override
            public boolean existsByEmail(Email email) {
                return users.stream().anyMatch(u -> email.equals(u.getEmail()));
            }

            @Override
            public void update(User user) {
                save(user);
            }
        };

        BuyerRepositoryPort buyerRepo = new BuyerRepositoryPort() {
            @Override
            public Buyer save(Buyer buyer) {
                buyers.removeIf(b -> b.getIdentification().equals(buyer.getIdentification()));
                buyers.add(buyer);
                return buyer;
            }

            @Override
            public Optional<Buyer> findByIdentification(Buyer buyer) {
                return buyers.stream()
                        .filter(b -> b.getIdentification().equals(buyer.getIdentification()))
                        .findFirst();
            }

            @Override
            public boolean existsByIdentification(DocumentId identification) {
                return buyers.stream().anyMatch(b -> identification.equals(b.getIdentification()));
            }

            @Override
            public boolean existsByEmail(Email email) {
                return buyers.stream().anyMatch(b -> email.equals(b.getEmail()));
            }

            @Override
            public void update(Buyer buyer) {
                save(buyer);
            }
        };

        SellerRepositoryPort sellerRepo = new SellerRepositoryPort() {
            @Override
            public Seller save(Seller seller) {
                sellers.removeIf(s -> s.getIdentification().equals(seller.getIdentification()));
                sellers.add(seller);
                return seller;
            }

            @Override
            public Optional<Seller> findByIdentification(Seller seller) {
                return sellers.stream()
                        .filter(s -> s.getIdentification().equals(seller.getIdentification()))
                        .findFirst();
            }

            @Override
            public boolean existsByIdentification(DocumentId identification) {
                return sellers.stream().anyMatch(s -> identification.equals(s.getIdentification()));
            }

            @Override
            public boolean existsByEmail(Email email) {
                return sellers.stream().anyMatch(s -> email.equals(s.getEmail()));
            }

            @Override
            public List<Seller> findAll() {
                return sellers;
            }

            @Override
            public void update(Seller seller) {
                save(seller);
            }
        };

        WarehouseRepositoryPort whRepo = new WarehouseRepositoryPort() {
            @Override
            public Warehouse save(Warehouse warehouse) {
                warehouses.removeIf(w -> w.getIdentifier().equals(warehouse.getIdentifier()));
                warehouses.add(warehouse);
                return warehouse;
            }

            @Override
            public Optional<Warehouse> findByIdentifier(Warehouse warehouse) {
                return warehouses.stream()
                        .filter(w -> w.getIdentifier().equals(warehouse.getIdentifier()))
                        .findFirst();
            }

            @Override
            public List<Warehouse> findByOwner(Seller owner) {
                return warehouses.stream()
                        .filter(w -> w.getOwner() != null
                                && w.getOwner().getIdentification().equals(owner.getIdentification()))
                        .toList();
            }

            @Override
            public void update(Warehouse warehouse) {
                save(warehouse);
            }
        };

        ProductRepositoryPort prodRepo = new ProductRepositoryPort() {
            @Override
            public Product save(Product product) {
                products.removeIf(p -> p.getIdentifier().equals(product.getIdentifier()));
                products.add(product);
                return product;
            }

            @Override
            public Optional<Product> findByIdentifier(Product product) {
                return products.stream()
                        .filter(p -> p.getIdentifier().equals(product.getIdentifier()))
                        .findFirst();
            }

            @Override
            public List<Product> findBySeller(Seller seller) {
                return products.stream()
                        .filter(p -> p.getSeller().getIdentification().equals(seller.getIdentification()))
                        .toList();
            }

            @Override
            public List<Product> findPublished() {
                return products.stream()
                        .filter(p -> application.domain.valueobjects.ProductStatus.PUBLISHED.equals(p.getStatus()))
                        .toList();
            }

            @Override
            public void update(Product product) {
                save(product);
            }
        };

        InventoryRepositoryPort invRepo = new InventoryRepositoryPort() {
            @Override
            public InventoryItem save(InventoryItem item) {
                if (item.getId() == null) {
                    item.setId(idSeq.getAndIncrement());
                }
                inventoryItems.removeIf(i -> i.getId().equals(item.getId()));
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
                return inventoryItems.stream()
                        .filter(i -> i.getProduct().getIdentifier().equals(product.getIdentifier()))
                        .toList();
            }

            @Override
            public void update(InventoryItem item) {
                save(item);
            }

            @Override
            public InventoryMovement saveMovement(InventoryMovement movement) {
                movement.setId(idSeq.getAndIncrement());
                movements.add(movement);
                return movement;
            }

            @Override
            public List<InventoryMovement> findMovements(InventoryItem item) {
                return movements.stream()
                        .filter(m -> m.getInventoryItem().getId().equals(item.getId()))
                        .toList();
            }
        };

        CartRepositoryPort cartRepo = new CartRepositoryPort() {
            @Override
            public application.domain.models.Cart save(application.domain.models.Cart cart) {
                if (cart.getId() == null) {
                    cart.setId(idSeq.getAndIncrement());
                }
                carts.removeIf(c -> c.getId().equals(cart.getId()));
                carts.add(cart);
                return cart;
            }

            @Override
            public Optional<application.domain.models.Cart> findActiveByBuyer(Buyer buyer) {
                return carts.stream()
                        .filter(c -> c.getBuyer().getIdentification().equals(buyer.getIdentification())
                                && CartStatus.ACTIVE.equals(c.getStatus()))
                        .findFirst();
            }

            @Override
            public Optional<application.domain.models.Cart> findById(application.domain.models.Cart cart) {
                return carts.stream().filter(c -> c.getId().equals(cart.getId())).findFirst();
            }

            @Override
            public void update(application.domain.models.Cart cart) {
                save(cart);
            }
        };

        OrderRepositoryPort orderRepo = new OrderRepositoryPort() {
            @Override
            public Order save(Order order) {
                orders.removeIf(o -> o.getIdentifier().equals(order.getIdentifier()));
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
            public List<Order> findByBuyer(Buyer buyer) {
                return orders.stream()
                        .filter(o -> o.getBuyer().getIdentification().equals(buyer.getIdentification()))
                        .toList();
            }

            @Override
            public void update(Order order) {
                save(order);
            }
        };

        OperationStore ops = new OperationStore();
        AuditStore audits = new AuditStore();
        RegisterOperationAndAuditService auditService = Fakes.auditService(ops, audits);
        ValidatePlatformUniquenessService uniqueness = Fakes.uniquenessService(userRepo, buyerRepo, sellerRepo);
        ValidateUserStatusService statusGuard = new ValidateUserStatusService();
        ValidateRoleAuthorizationService roleGuard = new ValidateRoleAuthorizationService();
        ValidateProductOwnershipService prodOwnerGuard = new ValidateProductOwnershipService();
        ValidateBuyerCanPurchaseService buyerPurchaseGuard = new ValidateBuyerCanPurchaseService();
        ValidateBuyerOwnsOrderService buyerOwnsOrderGuard = new ValidateBuyerOwnsOrderService();

        BCryptPasswordAdapter passwordAdapter = new BCryptPasswordAdapter();
        JjwtAdapter jwtAdapter = new JjwtAdapter(
                "MarketPlaceSuperSecretKeyForJWTSignature2024Minimum256BitsRequired!", 86400000L);

        // 1. Bootstrap initial ADMIN via DataSeeder
        DataSeeder seeder = new DataSeeder(
                userRepo, passwordAdapter, auditService,
                true, "admin", "admin123", "ADMIN-001", "admin@nexusmarket.com", "System Admin");
        seeder.run();

        LoginService loginService = new LoginService(userRepo, passwordAdapter, jwtAdapter);
        String adminToken = loginService.login("admin", "admin123");
        assertTrue(jwtAdapter.isTokenValid(adminToken));

        User adminProbe = new User();
        adminProbe.setUsername("admin");
        User admin = userRepo.findByUsername(adminProbe).orElseThrow();

        // 2. ADMIN onboards a SELLER + first Warehouse and registers a LOGISTICS_OPERATOR
        OnboardSellerService onboardSellerService = new OnboardSellerService(
                sellerRepo, whRepo, userRepo, passwordAdapter,
                statusGuard, roleGuard, uniqueness, auditService);

        Seller seller = new Seller();
        seller.setIdentification(new DocumentId("SELLER-001"));
        seller.setFullName("Tech Seller");
        seller.setEmail(new Email("seller@nexusmarket.com"));
        Warehouse sellerWh = new Warehouse();
        sellerWh.setIdentifier("WH-SELLER-1");
        sellerWh.setName("Seller Bogota Warehouse");
        sellerWh.setLocation("Bogota");
        User sellerAcc = new User();
        sellerAcc.setUsername("techseller");
        sellerAcc.setPassword("seller123");

        onboardSellerService.onboard(admin, seller, sellerWh, sellerAcc);
        User sellerUser = userRepo.findByUsername(sellerAcc).orElseThrow();

        RegisterUserService registerUserService = new RegisterUserService(
                userRepo, passwordAdapter, statusGuard, roleGuard, uniqueness, auditService);
        User operator = new User();
        operator.setIdentification(new DocumentId("OP-001"));
        operator.setFullName("Logistics Op");
        operator.setEmail(new Email("op@nexusmarket.com"));
        operator.setRole(UserRole.LOGISTICS_OPERATOR);
        operator.setUsername("logop");
        operator.setPassword("op123");
        User savedOperator = registerUserService.register(admin, operator);

        // 3. SELLER publishes a PhysicalProduct and a DigitalProduct and stocks the PhysicalProduct
        PublishProductService publishProductService = new PublishProductService(
                prodRepo, statusGuard, roleGuard, auditService);

        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Hardware");

        PhysicalProduct laptop = new PhysicalProduct();
        laptop.setIdentifier("PROD-LAPTOP");
        laptop.setName("Pro Laptop 15");
        laptop.setPrice(Money.of(BigDecimal.valueOf(1500)));
        laptop.setSeller(seller);
        laptop.setCategory(cat);
        publishProductService.publish(sellerUser, laptop);

        DigitalProduct license = new DigitalProduct();
        license.setIdentifier("PROD-ANTIVIRUS");
        license.setName("Security Suite Digital Key");
        license.setPrice(Money.of(BigDecimal.valueOf(50)));
        license.setSeller(seller);
        license.setCategory(cat);
        publishProductService.publish(sellerUser, license);

        ConsultCatalogService consultCatalogService = new ConsultCatalogService(prodRepo);
        assertEquals(2, consultCatalogService.consultPublished().size());

        RegisterInventoryEntryService entryService = new RegisterInventoryEntryService(
                invRepo, statusGuard, roleGuard, auditService);
        InventoryItem stockProbe = new InventoryItem();
        stockProbe.setProduct(laptop);
        stockProbe.setWarehouse(sellerWh);
        stockProbe.setCondition(InventoryItemCondition.AVAILABLE);
        InventoryItem stockedItem = entryService.registerEntry(sellerUser, stockProbe, 10);
        assertEquals(10, stockedItem.getStock().value());

        // 4. BUYER self-registers, adds both products to cart, and checks out
        RegisterBuyerService registerBuyerService = new RegisterBuyerService(
                buyerRepo, userRepo, passwordAdapter, uniqueness, auditService);
        Buyer buyer = new Buyer();
        buyer.setIdentification(new DocumentId("BUYER-001"));
        buyer.setFullName("Maria Compradora");
        buyer.setEmail(new Email("maria@nexusmarket.com"));
        buyer.setMainAddress("Carrera 43A # 1-50");
        User buyerAcc = new User();
        buyerAcc.setUsername("maria");
        buyerAcc.setPassword("maria123");
        Buyer registeredBuyer = registerBuyerService.register(buyer, buyerAcc);
        assertNotNull(loginService.login("maria", "maria123"));

        AddCartItemService addCartItemService = new AddCartItemService(
                cartRepo, buyerPurchaseGuard);
        CartItem item1 = new CartItem();
        item1.setProduct(laptop);
        item1.setQuantity(2);
        addCartItemService.addItem(registeredBuyer, item1);

        CartItem item2 = new CartItem();
        item2.setProduct(license);
        item2.setQuantity(1);
        addCartItemService.addItem(registeredBuyer, item2);

        ReserveStockForOrderItemService reserveForOrderItem = new ReserveStockForOrderItemService(
                invRepo, auditService);
        CheckoutCartService checkoutCartService = new CheckoutCartService(
                cartRepo, orderRepo, buyerPurchaseGuard, reserveForOrderItem, auditService);

        Order order = checkoutCartService.checkout(registeredBuyer);
        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());
        assertEquals(Money.of(BigDecimal.valueOf(3050)), order.getTotal());
        assertEquals(8, stockedItem.getStock().value());

        // 5. BUYER pays the order, LOGISTICS_OPERATOR dispatches and confirms delivery
        List<application.domain.models.Notification> sentNotifications = new ArrayList<>();
        ProcessOrderPaymentService paymentService = new ProcessOrderPaymentService(
                orderRepo,
                o -> PaymentResult.APPROVED,
                sentNotifications::add,
                buyerPurchaseGuard,
                buyerOwnsOrderGuard,
                auditService);

        Order paidOrder = paymentService.pay(registeredBuyer, order);
        assertEquals(OrderStatus.PAID, paidOrder.getStatus());
        assertEquals(1, sentNotifications.size());

        DispatchOrderService dispatchOrderService = new DispatchOrderService(
                orderRepo, invRepo, statusGuard, roleGuard, auditService);
        Order dispatchedOrder = dispatchOrderService.dispatch(savedOperator, paidOrder);
        assertEquals(OrderStatus.DISPATCHED, dispatchedOrder.getStatus());
        assertTrue(movements.stream().anyMatch(m -> InventoryMovementType.SALE_EXIT.equals(m.getType())));

        ConfirmDeliveryService confirmDeliveryService = new ConfirmDeliveryService(
                orderRepo, statusGuard, roleGuard, auditService);
        Order deliveredOrder = confirmDeliveryService.confirmDelivery(savedOperator, dispatchedOrder);
        assertEquals(OrderStatus.DELIVERED, deliveredOrder.getStatus());
        assertTrue(deliveredOrder.isFinalized());

        assertThrows(FinalizedOrderModificationException.class,
                () -> deliveredOrder.transitionTo(OrderStatus.DISPATCHED));

        // 6. ADMIN consults audit history
        ConsultAuditLogService consultAuditLogService = new ConsultAuditLogService(
                audits, statusGuard, roleGuard);
        List<AuditLog> auditHistory = consultAuditLogService.consult(admin, deliveredOrder);
        assertFalse(auditHistory.isEmpty());
    }
}
