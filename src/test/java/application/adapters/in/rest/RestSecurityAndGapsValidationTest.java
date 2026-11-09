package application.adapters.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import application.adapters.in.rest.controllers.CartController;
import application.adapters.in.rest.controllers.ProductController;
import application.adapters.in.rest.mappers.CartRestMapper;
import application.adapters.in.rest.mappers.ProductRestMapper;
import application.adapters.in.rest.requests.CartItemRequest;
import application.adapters.in.rest.requests.PublishProductRequest;
import application.adapters.in.rest.requests.RemoveCartItemRequest;
import application.adapters.out.persistence.mysql.adapters.InventoryMySqlAdapter;
import application.adapters.out.persistence.mysql.adapters.OperationMySqlAdapter;
import application.adapters.out.persistence.mysql.adapters.ProductMySqlAdapter;
import application.adapters.out.persistence.mysql.adapters.WarehouseMySqlAdapter;
import application.adapters.out.persistence.mysql.entities.CategoryJpaEntity;
import application.adapters.out.persistence.mysql.entities.OperationJpaEntity;
import application.adapters.out.persistence.mysql.entities.ProductJpaEntity;
import application.adapters.out.persistence.mysql.entities.SellerJpaEntity;
import application.adapters.out.persistence.mysql.entities.UserJpaEntity;
import application.adapters.out.persistence.mysql.mappers.CategoryMapper;
import application.adapters.out.persistence.mysql.mappers.InventoryMapper;
import application.adapters.out.persistence.mysql.mappers.OperationMapper;
import application.adapters.out.persistence.mysql.mappers.ProductMapper;
import application.adapters.out.persistence.mysql.mappers.SellerMapper;
import application.adapters.out.persistence.mysql.mappers.UserMapper;
import application.adapters.out.persistence.mysql.mappers.WarehouseMapper;
import application.adapters.out.persistence.mysql.repositories.CategoryJpaRepository;
import application.adapters.out.persistence.mysql.repositories.InventoryItemJpaRepository;
import application.adapters.out.persistence.mysql.repositories.InventoryMovementJpaRepository;
import application.adapters.out.persistence.mysql.repositories.OperationJpaRepository;
import application.adapters.out.persistence.mysql.repositories.ProductJpaRepository;
import application.adapters.out.persistence.mysql.repositories.SellerJpaRepository;
import application.adapters.out.persistence.mysql.repositories.UserJpaRepository;
import application.adapters.out.persistence.mysql.repositories.WarehouseJpaRepository;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Buyer;
import application.domain.models.Cart;
import application.domain.models.Category;
import application.domain.models.DigitalProduct;
import application.domain.models.InventoryItem;
import application.domain.models.Operation;
import application.domain.models.PhysicalProduct;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.in.AddCartItemUseCase;
import application.domain.ports.in.ChangeProductStatusUseCase;
import application.domain.ports.in.ClearCartUseCase;
import application.domain.ports.in.ConsultCartUseCase;
import application.domain.ports.in.ConsultCatalogUseCase;
import application.domain.ports.in.RemoveCartItemUseCase;
import application.domain.ports.in.UpdateProductUseCase;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.CartRepositoryPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.services.authorization.ValidateBuyerCanPurchaseService;
import application.domain.services.authorization.ValidateBuyerOwnershipService;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.services.buyer.UpdateBuyerService;
import application.domain.services.cart.AddCartItemService;
import application.domain.services.catalog.PublishProductService;
import application.domain.services.inventory.AdjustInventoryService;
import application.domain.services.inventory.RegisterInventoryEntryService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.support.Fakes;
import application.domain.support.Fakes.AuditStore;
import application.domain.support.Fakes.OperationStore;
import application.domain.valueobjects.BuyerCommercialStatus;
import application.domain.valueobjects.CartStatus;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.InventoryItemCondition;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.StockQuantity;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.UserStatus;
import application.domain.valueobjects.WarehouseType;
import application.infrastructure.security.AuthenticatedUserResolver;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * Validates all API security, ownership, and data-resolution edge cases discovered
 * during the REST & persistence audit.
 */
class RestSecurityAndGapsValidationTest {

    @Test
    void publishProductAutoResolvesSellerFromAuthenticatedRequesterAndBlocksImpersonation() {
        ProductRepositoryPort productRepo = mock(ProductRepositoryPort.class);
        when(productRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        RegisterOperationAndAuditService auditService = Fakes.auditService(new OperationStore(), new AuditStore());
        PublishProductService publishService = new PublishProductService(
                productRepo,
                new ValidateUserStatusService(),
                new ValidateRoleAuthorizationService(),
                auditService);

        User sellerUser = new User();
        sellerUser.setUsername("seller1");
        sellerUser.setIdentification(new DocumentId("SELLER-001"));
        sellerUser.setRole(UserRole.SELLER);
        sellerUser.setStatus(UserStatus.ACTIVE);

        AuthenticatedUserResolver resolver = mock(AuthenticatedUserResolver.class);
        when(resolver.resolveUser()).thenReturn(sellerUser);

        ProductController controller = new ProductController(
                publishService,
                mock(UpdateProductUseCase.class),
                mock(ChangeProductStatusUseCase.class),
                mock(ConsultCatalogUseCase.class),
                new ProductRestMapper(),
                resolver);

        // 1. Omitting sellerIdentification in request body auto-resolves from JWT requester
        var response = controller.publish(new PublishProductRequest(
                "PROD-AUTO", "Auto Seller Product", "Desc", "PHYSICAL",
                BigDecimal.valueOf(100), null, null, "Hardware", List.of()));
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("SELLER-001", response.getBody().data().sellerIdentification());

        // 2. Passing a different seller's identification is rejected with UnauthorizedOperationException
        assertThrows(UnauthorizedOperationException.class, () ->
                controller.publish(new PublishProductRequest(
                        "PROD-SPOOF", "Spoofed Product", "Desc", "PHYSICAL",
                        BigDecimal.valueOf(100), "OTHER-SELLER-999", null, "Hardware", List.of())));
    }

    @Test
    void cartRestMapperHydratesProductFromRepositoryAndSupportsRemovalWithoutQuantity() {
        ProductRepositoryPort productRepo = mock(ProductRepositoryPort.class);
        PhysicalProduct storedProduct = new PhysicalProduct();
        storedProduct.setIdentifier("PROD-100");
        storedProduct.setName("Mechanical Keyboard");
        storedProduct.setPrice(Money.of(BigDecimal.valueOf(120)));
        storedProduct.setStatus(ProductStatus.PUBLISHED);
        when(productRepo.findByIdentifier(any())).thenReturn(Optional.of(storedProduct));

        CartRestMapper cartRestMapper = new CartRestMapper(productRepo);

        Buyer buyer = new Buyer();
        buyer.setIdentification(new DocumentId("BUYER-100"));
        buyer.setCommercialStatus(BuyerCommercialStatus.ACTIVE);

        Cart activeCart = new Cart();
        activeCart.setId(1L);
        activeCart.setBuyer(buyer);
        activeCart.setStatus(CartStatus.ACTIVE);

        CartRepositoryPort cartRepo = mock(CartRepositoryPort.class);
        when(cartRepo.findActiveByBuyer(buyer)).thenReturn(Optional.of(activeCart));

        AddCartItemUseCase addCartItemUseCase = new AddCartItemService(
                cartRepo, new ValidateBuyerCanPurchaseService());
        RemoveCartItemUseCase removeCartItemUseCase = mock(RemoveCartItemUseCase.class);
        when(removeCartItemUseCase.removeItem(any(), any())).thenReturn(activeCart);

        AuthenticatedUserResolver resolver = mock(AuthenticatedUserResolver.class);
        when(resolver.resolveBuyer()).thenReturn(buyer);

        CartController cartController = new CartController(
                addCartItemUseCase,
                removeCartItemUseCase,
                mock(ClearCartUseCase.class),
                mock(ConsultCartUseCase.class),
                cartRestMapper,
                resolver);

        // Adding item via REST hydrates PUBLISHED product and succeeds
        var addResp = cartController.addItem(new CartItemRequest("PROD-100", "Switch", "Red", 2));
        assertEquals(HttpStatus.OK, addResp.getStatusCode());
        assertEquals("Mechanical Keyboard", addResp.getBody().data().items().get(0).productName());

        // Removing item by body without quantity and by path both succeed
        var remBodyResp = cartController.removeItem(new RemoveCartItemRequest("PROD-100", "Switch", "Red"));
        assertEquals(HttpStatus.OK, remBodyResp.getStatusCode());
        var remPathResp = cartController.removeItemByPath("PROD-100", "Switch", "Red");
        assertEquals(HttpStatus.OK, remPathResp.getStatusCode());

        // Non-existent product throws EntityNotFoundException
        when(productRepo.findByIdentifier(any())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                cartController.addItem(new CartItemRequest("UNKNOWN-PROD", null, null, 1)));
    }

    @Test
    void digitalProductInventoryIsRejectedAndConditionUpdatesOnAdjustment() {
        RegisterOperationAndAuditService auditService = Fakes.auditService(new OperationStore(), new AuditStore());
        InventoryRepositoryPort invRepo = mock(InventoryRepositoryPort.class);

        RegisterInventoryEntryService entryService = new RegisterInventoryEntryService(
                invRepo, new ValidateUserStatusService(), new ValidateRoleAuthorizationService(), auditService);

        User operator = new User();
        operator.setUsername("op");
        operator.setRole(UserRole.LOGISTICS_OPERATOR);
        operator.setStatus(UserStatus.ACTIVE);

        InventoryItem digitalProbe = new InventoryItem();
        digitalProbe.setProduct(new DigitalProduct());
        digitalProbe.setWarehouse(new Warehouse());

        assertThrows(DomainException.class, () -> entryService.registerEntry(operator, digitalProbe, 5));

        InventoryItem storedItem = new InventoryItem();
        storedItem.setId(1L);
        storedItem.setProduct(new PhysicalProduct());
        storedItem.setWarehouse(new Warehouse());
        storedItem.setStock(StockQuantity.of(10));
        storedItem.setCondition(InventoryItemCondition.AVAILABLE);
        when(invRepo.findByProductAndWarehouse(any())).thenReturn(Optional.of(storedItem));
        when(invRepo.saveMovement(any())).thenAnswer(inv -> inv.getArgument(0));

        AdjustInventoryService adjustService = new AdjustInventoryService(
                invRepo, new ValidateUserStatusService(), new ValidateRoleAuthorizationService(), auditService);
        InventoryItem adjustProbe = new InventoryItem();
        adjustProbe.setProduct(new PhysicalProduct());
        adjustProbe.setWarehouse(new Warehouse());
        adjustProbe.setCondition(InventoryItemCondition.DAMAGED);

        adjustService.adjust(operator, adjustProbe, 4);
        assertEquals(4, storedItem.getStock().value());
        assertEquals(InventoryItemCondition.DAMAGED, storedItem.getCondition());
    }

    @Test
    void updateBuyerUpdatesEmailAndMySqlAdaptersResolveForeignKeysSafely() {
        BuyerRepositoryPort buyerRepo = mock(BuyerRepositoryPort.class);
        Buyer storedBuyer = new Buyer();
        storedBuyer.setIdentification(new DocumentId("B-1"));
        storedBuyer.setEmail(new Email("old@test.com"));
        storedBuyer.setMainAddress("Old Addr");
        when(buyerRepo.findByIdentification(any())).thenReturn(Optional.of(storedBuyer));

        UpdateBuyerService updateBuyerService = new UpdateBuyerService(
                buyerRepo, new ValidateUserStatusService(), new ValidateBuyerOwnershipService());
        User buyerUser = new User();
        buyerUser.setIdentification(new DocumentId("B-1"));
        buyerUser.setRole(UserRole.BUYER);
        buyerUser.setStatus(UserStatus.ACTIVE);

        Buyer updatePatch = new Buyer();
        updatePatch.setIdentification(new DocumentId("B-1"));
        updatePatch.setFullName("New Name");
        updatePatch.setEmail(new Email("new@test.com"));
        updatePatch.setMainAddress("New Addr");

        Buyer updated = updateBuyerService.update(buyerUser, updatePatch);
        assertEquals("new@test.com", updated.getEmail().value());

        // Verify ProductMySqlAdapter auto-saves new Category by name and checks Seller existence
        ProductJpaRepository prodJpa = mock(ProductJpaRepository.class);
        CategoryJpaRepository catJpa = mock(CategoryJpaRepository.class);
        SellerJpaRepository sellerJpa = mock(SellerJpaRepository.class);
        UserMapper userMapper = new UserMapper();
        SellerMapper sellerMapper = new SellerMapper(userMapper);
        CategoryMapper categoryMapper = new CategoryMapper();
        ProductMapper productMapper = new ProductMapper(sellerMapper, categoryMapper);

        ProductMySqlAdapter productAdapter = new ProductMySqlAdapter(prodJpa, productMapper, catJpa, sellerJpa);
        SellerJpaEntity managedSeller = new SellerJpaEntity();
        managedSeller.setIdentification("S-1");
        managedSeller.setFullName("Seller 1");
        managedSeller.setEmail("s1@test.com");
        managedSeller.setRole("SELLER");
        managedSeller.setStatus("ACTIVE");
        when(sellerJpa.findById("S-1")).thenReturn(Optional.of(managedSeller));
        when(catJpa.findByName("Gaming")).thenReturn(Optional.empty());
        when(catJpa.save(any(CategoryJpaEntity.class))).thenAnswer(inv -> {
            CategoryJpaEntity c = inv.getArgument(0);
            c.setId(99L);
            return c;
        });
        when(prodJpa.save(any(ProductJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PhysicalProduct p = new PhysicalProduct();
        p.setIdentifier("P-99");
        p.setName("Headset");
        p.setPrice(Money.of(BigDecimal.TEN));
        Seller sStub = new Seller();
        sStub.setIdentification(new DocumentId("S-1"));
        p.setSeller(sStub);
        Category cStub = new Category();
        cStub.setName("Gaming");
        p.setCategory(cStub);

        Product savedProd = productAdapter.save(p);
        assertEquals(99L, savedProd.getCategory().getId());

        // Verify WarehouseMySqlAdapter rejects unknown Seller on SELLER warehouse
        WarehouseJpaRepository whJpa = mock(WarehouseJpaRepository.class);
        WarehouseMySqlAdapter whAdapter = new WarehouseMySqlAdapter(whJpa, new WarehouseMapper(sellerMapper), sellerJpa);
        when(sellerJpa.findById("MISSING-SELLER")).thenReturn(Optional.empty());
        Warehouse badWh = new Warehouse();
        badWh.setIdentifier("WH-BAD");
        badWh.setName("Bad");
        badWh.setType(WarehouseType.SELLER);
        Seller missingSeller = new Seller();
        missingSeller.setIdentification(new DocumentId("MISSING-SELLER"));
        badWh.setOwner(missingSeller);
        assertThrows(EntityNotFoundException.class, () -> whAdapter.save(badWh));

        // Verify InventoryMySqlAdapter rejects DIGITAL product in JPA
        InventoryItemJpaRepository invItemJpa = mock(InventoryItemJpaRepository.class);
        InventoryMovementJpaRepository invMovJpa = mock(InventoryMovementJpaRepository.class);
        UserJpaRepository userJpa = mock(UserJpaRepository.class);
        InventoryMySqlAdapter invAdapter = new InventoryMySqlAdapter(
                invItemJpa, invMovJpa,
                new InventoryMapper(productMapper, new WarehouseMapper(sellerMapper), userMapper),
                prodJpa, whJpa, userJpa);
        ProductJpaEntity digitalEntity = new ProductJpaEntity();
        digitalEntity.setIdentifier("P-DIG");
        digitalEntity.setProductType("DIGITAL");
        when(prodJpa.findById("P-DIG")).thenReturn(Optional.of(digitalEntity));
        InventoryItem badInv = new InventoryItem();
        PhysicalProduct digProbe = new PhysicalProduct();
        digProbe.setIdentifier("P-DIG");
        badInv.setProduct(digProbe);
        assertThrows(DomainException.class, () -> invAdapter.save(badInv));

        // Verify OperationMySqlAdapter resolves userId by username when userId is null
        OperationJpaRepository opJpa = mock(OperationJpaRepository.class);
        UserJpaEntity managedUser = new UserJpaEntity();
        managedUser.setUserId(77);
        managedUser.setUsername("admin");
        when(userJpa.findByUsername("admin")).thenReturn(Optional.of(managedUser));
        when(opJpa.save(any(OperationJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        OperationMySqlAdapter opAdapter = new OperationMySqlAdapter(opJpa, new OperationMapper(userMapper), userJpa);
        User actorWithoutId = new User();
        actorWithoutId.setUsername("admin");
        Operation op = Operation.of(OperationType.USER_REGISTRATION, actorWithoutId, actorWithoutId);
        Operation savedOp = opAdapter.save(op);
        assertEquals(Integer.valueOf(77), savedOp.getPerformedBy().getUserId());
    }
}
