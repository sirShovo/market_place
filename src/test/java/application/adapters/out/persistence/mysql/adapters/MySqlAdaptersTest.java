package application.adapters.out.persistence.mysql.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.adapters.out.persistence.mysql.entities.BuyerJpaEntity;
import application.adapters.out.persistence.mysql.entities.CartJpaEntity;
import application.adapters.out.persistence.mysql.entities.CategoryJpaEntity;
import application.adapters.out.persistence.mysql.entities.InventoryItemJpaEntity;
import application.adapters.out.persistence.mysql.entities.OperationJpaEntity;
import application.adapters.out.persistence.mysql.entities.OrderJpaEntity;
import application.adapters.out.persistence.mysql.entities.ProductJpaEntity;
import application.adapters.out.persistence.mysql.entities.SellerJpaEntity;
import application.adapters.out.persistence.mysql.entities.UserJpaEntity;
import application.adapters.out.persistence.mysql.entities.WarehouseJpaEntity;
import application.adapters.out.persistence.mysql.mappers.BuyerMapper;
import application.adapters.out.persistence.mysql.mappers.CartMapper;
import application.adapters.out.persistence.mysql.mappers.CategoryMapper;
import application.adapters.out.persistence.mysql.mappers.InventoryMapper;
import application.adapters.out.persistence.mysql.mappers.OperationMapper;
import application.adapters.out.persistence.mysql.mappers.OrderMapper;
import application.adapters.out.persistence.mysql.mappers.ProductMapper;
import application.adapters.out.persistence.mysql.mappers.SellerMapper;
import application.adapters.out.persistence.mysql.mappers.UserMapper;
import application.adapters.out.persistence.mysql.mappers.WarehouseMapper;
import application.adapters.out.persistence.mysql.repositories.BuyerJpaRepository;
import application.adapters.out.persistence.mysql.repositories.CartJpaRepository;
import application.adapters.out.persistence.mysql.repositories.CategoryJpaRepository;
import application.adapters.out.persistence.mysql.repositories.InventoryItemJpaRepository;
import application.adapters.out.persistence.mysql.repositories.InventoryMovementJpaRepository;
import application.adapters.out.persistence.mysql.repositories.OperationJpaRepository;
import application.adapters.out.persistence.mysql.repositories.OrderJpaRepository;
import application.adapters.out.persistence.mysql.repositories.ProductJpaRepository;
import application.adapters.out.persistence.mysql.repositories.SellerJpaRepository;
import application.adapters.out.persistence.mysql.repositories.UserJpaRepository;
import application.adapters.out.persistence.mysql.repositories.WarehouseJpaRepository;
import application.domain.models.Buyer;
import application.domain.models.Cart;
import application.domain.models.Category;
import application.domain.models.InventoryItem;
import application.domain.models.Operation;
import application.domain.models.Order;
import application.domain.models.PhysicalProduct;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.valueobjects.BuyerCommercialStatus;
import application.domain.valueobjects.CartStatus;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.InventoryItemCondition;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.SellerStatus;
import application.domain.valueobjects.StockQuantity;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.UserStatus;
import application.domain.valueobjects.WarehouseType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MySqlAdaptersTest {

    private final UserMapper userMapper = new UserMapper();
    private final BuyerMapper buyerMapper = new BuyerMapper();
    private final SellerMapper sellerMapper = new SellerMapper(userMapper);
    private final WarehouseMapper warehouseMapper = new WarehouseMapper(sellerMapper);
    private final CategoryMapper categoryMapper = new CategoryMapper();
    private final ProductMapper productMapper = new ProductMapper(sellerMapper, categoryMapper);
    private final InventoryMapper inventoryMapper = new InventoryMapper(productMapper, warehouseMapper, userMapper);
    private final CartMapper cartMapper = new CartMapper(buyerMapper, productMapper);
    private final OrderMapper orderMapper = new OrderMapper(buyerMapper, productMapper, warehouseMapper);
    private final OperationMapper operationMapper = new OperationMapper(userMapper);

    @Test
    void userBuyerSellerAndWarehouseAdaptersDelegateCorrectly() {
        UserJpaRepository userRepo = mock(UserJpaRepository.class);
        UserMySqlAdapter userAdapter = new UserMySqlAdapter(userRepo, userMapper);

        User user = new User();
        user.setUsername("u1");
        user.setPassword("p");
        user.setIdentification(new DocumentId("D1"));
        user.setFullName("User One");
        user.setEmail(new Email("u1@test.com"));
        user.setRole(UserRole.BUYER);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepo.save(any(UserJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepo.existsByIdentification("D1")).thenReturn(true);
        assertEquals("u1", userAdapter.save(user).getUsername());
        assertTrue(userAdapter.existsByIdentification(new DocumentId("D1")));

        BuyerJpaRepository buyerRepo = mock(BuyerJpaRepository.class);
        BuyerMySqlAdapter buyerAdapter = new BuyerMySqlAdapter(buyerRepo, buyerMapper);
        Buyer buyer = new Buyer();
        buyer.setIdentification(new DocumentId("B1"));
        buyer.setFullName("Buyer One");
        buyer.setEmail(new Email("b1@test.com"));
        buyer.setRole(UserRole.BUYER);
        buyer.setCommercialStatus(BuyerCommercialStatus.ACTIVE);
        buyer.setMainAddress("Addr");

        when(buyerRepo.save(any(BuyerJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(new DocumentId("B1"), buyerAdapter.save(buyer).getIdentification());

        SellerJpaRepository sellerRepo = mock(SellerJpaRepository.class);
        WarehouseJpaRepository whRepo = mock(WarehouseJpaRepository.class);
        SellerMySqlAdapter sellerAdapter = new SellerMySqlAdapter(
                sellerRepo, whRepo, sellerMapper, warehouseMapper);

        Seller seller = new Seller();
        seller.setIdentification(new DocumentId("S1"));
        seller.setFullName("Seller One");
        seller.setEmail(new Email("s1@test.com"));
        seller.setRole(UserRole.SELLER);
        seller.setStatus(SellerStatus.ACTIVE);

        when(sellerRepo.findById("S1")).thenReturn(Optional.of(sellerMapper.toEntity(seller)));
        when(whRepo.findByOwner_Identification("S1")).thenReturn(List.of());
        assertTrue(sellerAdapter.findByIdentification(seller).isPresent());

        WarehouseMySqlAdapter whAdapter = new WarehouseMySqlAdapter(whRepo, warehouseMapper);
        Warehouse wh = new Warehouse();
        wh.setIdentifier("WH1");
        wh.setName("Main");
        wh.setType(WarehouseType.SELLER);
        wh.setOwner(seller);
        when(whRepo.save(any(WarehouseJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        assertEquals("WH1", whAdapter.save(wh).getIdentifier());
    }

    @Test
    void categoryProductInventoryCartOrderAndOperationAdaptersDelegateCorrectly() {
        CategoryJpaRepository catRepo = mock(CategoryJpaRepository.class);
        CategoryMySqlAdapter catAdapter = new CategoryMySqlAdapter(catRepo, categoryMapper);
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Books");
        when(catRepo.save(any(CategoryJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        assertEquals("Books", catAdapter.save(cat).getName());

        ProductJpaRepository prodRepo = mock(ProductJpaRepository.class);
        ProductMySqlAdapter prodAdapter = new ProductMySqlAdapter(prodRepo, productMapper);
        PhysicalProduct prod = new PhysicalProduct();
        prod.setIdentifier("P1");
        prod.setName("Book");
        prod.setPrice(Money.of(BigDecimal.TEN));
        prod.setStatus(ProductStatus.PUBLISHED);
        when(prodRepo.findByStatus("PUBLISHED")).thenReturn(List.of(productMapper.toEntity(prod)));
        List<Product> published = prodAdapter.findPublished();
        assertEquals(1, published.size());

        InventoryItemJpaRepository invItemRepo = mock(InventoryItemJpaRepository.class);
        InventoryMovementJpaRepository invMovRepo = mock(InventoryMovementJpaRepository.class);
        InventoryMySqlAdapter invAdapter = new InventoryMySqlAdapter(
                invItemRepo, invMovRepo, inventoryMapper);

        Warehouse wh = new Warehouse();
        wh.setIdentifier("WH1");
        wh.setName("Main");
        wh.setType(WarehouseType.MARKETPLACE);

        InventoryItem item = new InventoryItem();
        item.setId(10L);
        item.setProduct(prod);
        item.setWarehouse(wh);
        item.setStock(StockQuantity.of(5));
        item.setCondition(InventoryItemCondition.AVAILABLE);
        when(invItemRepo.save(any(InventoryItemJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(5, invAdapter.save(item).getStock().value());

        CartJpaRepository cartRepo = mock(CartJpaRepository.class);
        CartMySqlAdapter cartAdapter = new CartMySqlAdapter(cartRepo, cartMapper);
        Buyer buyer = new Buyer();
        buyer.setIdentification(new DocumentId("B1"));
        buyer.setFullName("Buyer");
        buyer.setEmail(new Email("b1@test.com"));
        buyer.setRole(UserRole.BUYER);
        buyer.setCommercialStatus(BuyerCommercialStatus.ACTIVE);
        buyer.setMainAddress("Addr");

        Cart cart = new Cart();
        cart.setId(2L);
        cart.setBuyer(buyer);
        cart.setStatus(CartStatus.ACTIVE);
        when(cartRepo.findByBuyer_IdentificationAndStatus("B1", "ACTIVE"))
                .thenReturn(Optional.of(cartMapper.toEntity(cart)));
        assertTrue(cartAdapter.findActiveByBuyer(buyer).isPresent());

        OrderJpaRepository orderRepo = mock(OrderJpaRepository.class);
        OrderMySqlAdapter orderAdapter = new OrderMySqlAdapter(orderRepo, orderMapper);
        Order order = new Order();
        order.setIdentifier("ORD-1");
        order.setBuyer(buyer);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setCreatedAt(LocalDateTime.now());
        when(orderRepo.findByBuyer_Identification("B1"))
                .thenReturn(List.of(orderMapper.toEntity(order)));
        assertEquals(1, orderAdapter.findByBuyer(buyer).size());

        OperationJpaRepository opRepo = mock(OperationJpaRepository.class);
        OperationMySqlAdapter opAdapter = new OperationMySqlAdapter(opRepo, operationMapper);
        Operation op = Operation.of(OperationType.CART_CHECKOUT, null, order);
        when(opRepo.save(any(OperationJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(OperationType.CART_CHECKOUT, opAdapter.save(op).getOperationType());
        verify(opRepo).save(any(OperationJpaEntity.class));
    }
}
