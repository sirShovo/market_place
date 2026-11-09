package application.adapters.out.persistence.mysql.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import application.adapters.out.persistence.mysql.entities.BuyerJpaEntity;
import application.adapters.out.persistence.mysql.entities.CartJpaEntity;
import application.adapters.out.persistence.mysql.entities.CategoryJpaEntity;
import application.adapters.out.persistence.mysql.entities.InventoryItemJpaEntity;
import application.adapters.out.persistence.mysql.entities.InventoryMovementJpaEntity;
import application.adapters.out.persistence.mysql.entities.OperationJpaEntity;
import application.adapters.out.persistence.mysql.entities.OrderJpaEntity;
import application.adapters.out.persistence.mysql.entities.ProductJpaEntity;
import application.adapters.out.persistence.mysql.entities.SellerJpaEntity;
import application.adapters.out.persistence.mysql.entities.UserJpaEntity;
import application.adapters.out.persistence.mysql.entities.WarehouseJpaEntity;
import application.domain.models.Buyer;
import application.domain.models.Cart;
import application.domain.models.CartItem;
import application.domain.models.Category;
import application.domain.models.DigitalProduct;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Operation;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.PhysicalProduct;
import application.domain.models.Product;
import application.domain.models.ProductVariant;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.valueobjects.BuyerCommercialStatus;
import application.domain.valueobjects.CartStatus;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.InventoryItemCondition;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.ProductType;
import application.domain.valueobjects.SellerStatus;
import application.domain.valueobjects.StockQuantity;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.UserStatus;
import application.domain.valueobjects.WarehouseType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PersistenceMappersTest {

    private UserMapper userMapper;
    private BuyerMapper buyerMapper;
    private SellerMapper sellerMapper;
    private WarehouseMapper warehouseMapper;
    private CategoryMapper categoryMapper;
    private ProductMapper productMapper;
    private InventoryMapper inventoryMapper;
    private CartMapper cartMapper;
    private OrderMapper orderMapper;
    private OperationMapper operationMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
        buyerMapper = new BuyerMapper();
        sellerMapper = new SellerMapper(userMapper);
        warehouseMapper = new WarehouseMapper(sellerMapper);
        categoryMapper = new CategoryMapper();
        productMapper = new ProductMapper(sellerMapper, categoryMapper);
        inventoryMapper = new InventoryMapper(productMapper, warehouseMapper, userMapper);
        cartMapper = new CartMapper(buyerMapper, productMapper);
        orderMapper = new OrderMapper(buyerMapper, productMapper, warehouseMapper);
        operationMapper = new OperationMapper(userMapper);
    }

    @Test
    void userBuyerSellerAndWarehouseRoundTrip() {
        User admin = new User();
        admin.setUserId(1);
        admin.setUsername("admin");
        admin.setPassword("hashed");
        admin.setIdentification(new DocumentId("DOC-ADMIN"));
        admin.setFullName("Admin User");
        admin.setEmail(new Email("admin@test.com"));
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);

        UserJpaEntity userEntity = userMapper.toEntity(admin);
        User rehydratedUser = userMapper.toDomain(userEntity);
        assertEquals(admin.getUsername(), rehydratedUser.getUsername());
        assertEquals(UserRole.ADMIN, rehydratedUser.getRole());
        assertEquals(UserStatus.ACTIVE, rehydratedUser.getStatus());

        Buyer buyer = new Buyer();
        buyer.setIdentification(new DocumentId("DOC-BUYER"));
        buyer.setFullName("Buyer One");
        buyer.setEmail(new Email("buyer@test.com"));
        buyer.setRole(UserRole.BUYER);
        buyer.setCommercialStatus(BuyerCommercialStatus.ACTIVE);
        buyer.setMainAddress("Calle 1");
        buyer.setAdditionalAddresses(List.of("Calle 2"));

        BuyerJpaEntity buyerEntity = buyerMapper.toEntity(buyer);
        Buyer rehydratedBuyer = buyerMapper.toDomain(buyerEntity);
        assertEquals(buyer.getIdentification(), rehydratedBuyer.getIdentification());
        assertEquals(BuyerCommercialStatus.ACTIVE, rehydratedBuyer.getCommercialStatus());
        assertEquals(1, rehydratedBuyer.getAdditionalAddresses().size());

        Seller seller = new Seller();
        seller.setIdentification(new DocumentId("DOC-SELLER"));
        seller.setFullName("Seller One");
        seller.setEmail(new Email("seller@test.com"));
        seller.setRole(UserRole.SELLER);
        seller.setStatus(SellerStatus.ACTIVE);
        seller.setOnboardedBy(admin);

        SellerJpaEntity sellerEntity = sellerMapper.toEntity(seller);
        Seller rehydratedSeller = sellerMapper.toDomain(sellerEntity);
        assertEquals(seller.getIdentification(), rehydratedSeller.getIdentification());
        assertEquals(SellerStatus.ACTIVE, rehydratedSeller.getStatus());
        assertNotNull(rehydratedSeller.getOnboardedBy());

        Warehouse warehouse = new Warehouse();
        warehouse.setIdentifier("WH-1");
        warehouse.setName("Main Warehouse");
        warehouse.setType(WarehouseType.SELLER);
        warehouse.setOwner(seller);
        warehouse.setLocation("Medellin");

        WarehouseJpaEntity whEntity = warehouseMapper.toEntity(warehouse);
        Warehouse rehydratedWh = warehouseMapper.toDomain(whEntity);
        assertEquals("WH-1", rehydratedWh.getIdentifier());
        assertEquals(WarehouseType.SELLER, rehydratedWh.getType());
        assertEquals(seller.getIdentification(), rehydratedWh.getOwner().getIdentification());
    }

    @Test
    void productPolymorphismAndInventoryRoundTrip() {
        Category cat = new Category();
        cat.setId(10L);
        cat.setName("Tech");
        CategoryJpaEntity catEntity = categoryMapper.toEntity(cat);
        assertEquals("Tech", categoryMapper.toDomain(catEntity).getName());

        Seller seller = new Seller();
        seller.setIdentification(new DocumentId("DOC-SELLER"));
        seller.setFullName("Seller One");
        seller.setEmail(new Email("seller@test.com"));
        seller.setRole(UserRole.SELLER);
        seller.setStatus(SellerStatus.ACTIVE);

        ProductVariant variant = new ProductVariant();
        variant.setAttributeName("Color");
        variant.setValue("Black");

        PhysicalProduct physical = new PhysicalProduct();
        physical.setIdentifier("PROD-PHYS");
        physical.setName("Keyboard");
        physical.setPrice(Money.of(BigDecimal.valueOf(120)));
        physical.setStatus(ProductStatus.PUBLISHED);
        physical.setSeller(seller);
        physical.setCategory(cat);
        physical.setVariants(List.of(variant));

        ProductJpaEntity physEntity = productMapper.toEntity(physical);
        Product rehydratedPhys = productMapper.toDomain(physEntity);
        assertInstanceOf(PhysicalProduct.class, rehydratedPhys);
        assertEquals(ProductType.PHYSICAL, rehydratedPhys.getType());
        assertEquals(1, rehydratedPhys.getVariants().size());

        DigitalProduct digital = new DigitalProduct();
        digital.setIdentifier("PROD-DIG");
        digital.setName("E-Book");
        digital.setPrice(Money.of(BigDecimal.valueOf(25)));
        digital.setStatus(ProductStatus.PUBLISHED);
        digital.setSeller(seller);
        digital.setCategory(cat);

        ProductJpaEntity digEntity = productMapper.toEntity(digital);
        Product rehydratedDig = productMapper.toDomain(digEntity);
        assertInstanceOf(DigitalProduct.class, rehydratedDig);
        assertEquals(ProductType.DIGITAL, rehydratedDig.getType());

        Warehouse warehouse = new Warehouse();
        warehouse.setIdentifier("WH-1");
        warehouse.setName("Main Warehouse");
        warehouse.setType(WarehouseType.SELLER);
        warehouse.setOwner(seller);

        InventoryItem item = new InventoryItem();
        item.setId(5L);
        item.setProduct(physical);
        item.setWarehouse(warehouse);
        item.setStock(StockQuantity.of(15));
        item.setCondition(InventoryItemCondition.AVAILABLE);

        InventoryItemJpaEntity itemEntity = inventoryMapper.toEntity(item);
        InventoryItem rehydratedItem = inventoryMapper.toDomain(itemEntity);
        assertEquals(15, rehydratedItem.getStock().value());
        assertEquals(InventoryItemCondition.AVAILABLE, rehydratedItem.getCondition());

        InventoryMovement movement = InventoryMovement.of(
                rehydratedItem, InventoryMovementType.ENTRY, 15, null);
        movement.setId(99L);
        InventoryMovementJpaEntity movEntity = inventoryMapper.toMovementEntity(movement);
        InventoryMovement rehydratedMov = inventoryMapper.toMovementDomain(movEntity);
        assertEquals(InventoryMovementType.ENTRY, rehydratedMov.getType());
        assertEquals(15, rehydratedMov.getQuantity());
    }

    @Test
    void cartOrderAndOperationRoundTrip() {
        Buyer buyer = new Buyer();
        buyer.setIdentification(new DocumentId("DOC-BUYER"));
        buyer.setFullName("Buyer One");
        buyer.setEmail(new Email("buyer@test.com"));
        buyer.setRole(UserRole.BUYER);
        buyer.setCommercialStatus(BuyerCommercialStatus.ACTIVE);
        buyer.setMainAddress("Calle 1");

        Seller seller = new Seller();
        seller.setIdentification(new DocumentId("DOC-SELLER"));
        seller.setFullName("Seller One");
        seller.setEmail(new Email("seller@test.com"));
        seller.setRole(UserRole.SELLER);
        seller.setStatus(SellerStatus.ACTIVE);

        PhysicalProduct physical = new PhysicalProduct();
        physical.setIdentifier("PROD-1");
        physical.setName("Laptop");
        physical.setPrice(Money.of(BigDecimal.valueOf(1000)));
        physical.setStatus(ProductStatus.PUBLISHED);
        physical.setSeller(seller);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(physical);
        cartItem.setQuantity(2);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setBuyer(buyer);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setItems(List.of(cartItem));

        CartJpaEntity cartEntity = cartMapper.toEntity(cart);
        Cart rehydratedCart = cartMapper.toDomain(cartEntity);
        assertEquals(CartStatus.ACTIVE, rehydratedCart.getStatus());
        assertEquals(1, rehydratedCart.getItems().size());

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(physical);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(Money.of(BigDecimal.valueOf(1000)));

        Order order = new Order();
        order.setIdentifier("ORD-100");
        order.setBuyer(buyer);
        order.addItem(orderItem);
        order.setStatus(OrderStatus.DELIVERED);
        order.setCreatedAt(LocalDateTime.now());

        OrderJpaEntity orderEntity = orderMapper.toEntity(order);
        Order rehydratedOrder = orderMapper.toDomain(orderEntity);
        assertEquals(OrderStatus.DELIVERED, rehydratedOrder.getStatus());
        assertEquals(Money.of(BigDecimal.valueOf(2000)), rehydratedOrder.getTotal());
        assertEquals(1, rehydratedOrder.getItems().size());

        Operation op = Operation.of(OperationType.ORDER_DELIVERY, null, order);
        op.setOperationId(7);
        OperationJpaEntity opEntity = operationMapper.toEntity(op);
        Operation rehydratedOp = operationMapper.toDomain(opEntity);
        assertEquals(OperationType.ORDER_DELIVERY, rehydratedOp.getOperationType());
        assertEquals("Order", rehydratedOp.getAffectedEntity().auditableType());
        assertEquals("ORD-100", rehydratedOp.getAffectedEntity().auditableId());
    }
}
