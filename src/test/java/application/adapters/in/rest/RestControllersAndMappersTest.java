package application.adapters.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import application.adapters.in.rest.controllers.AuditController;
import application.adapters.in.rest.controllers.AuthController;
import application.adapters.in.rest.controllers.BuyerController;
import application.adapters.in.rest.controllers.CartController;
import application.adapters.in.rest.controllers.InventoryController;
import application.adapters.in.rest.controllers.OrderController;
import application.adapters.in.rest.controllers.ProductController;
import application.adapters.in.rest.controllers.SellerController;
import application.adapters.in.rest.controllers.UserController;
import application.adapters.in.rest.controllers.WarehouseController;
import application.adapters.in.rest.mappers.AuditRestMapper;
import application.adapters.in.rest.mappers.BuyerRestMapper;
import application.adapters.in.rest.mappers.CartRestMapper;
import application.adapters.in.rest.mappers.InventoryRestMapper;
import application.adapters.in.rest.mappers.OrderRestMapper;
import application.adapters.in.rest.mappers.ProductRestMapper;
import application.adapters.in.rest.mappers.SellerRestMapper;
import application.adapters.in.rest.mappers.UserRestMapper;
import application.adapters.in.rest.mappers.WarehouseRestMapper;
import application.adapters.in.rest.requests.CartItemRequest;
import application.adapters.in.rest.requests.ChangeStatusRequest;
import application.adapters.in.rest.requests.InventoryOperationRequest;
import application.adapters.in.rest.requests.LoginRequest;
import application.adapters.in.rest.requests.OnboardSellerRequest;
import application.adapters.in.rest.requests.ProductVariantDto;
import application.adapters.in.rest.requests.PublishProductRequest;
import application.adapters.in.rest.requests.RegisterBuyerRequest;
import application.adapters.in.rest.requests.RegisterUserRequest;
import application.adapters.in.rest.requests.RegisterWarehouseRequest;
import application.domain.enums.AuditSeverity;
import application.domain.exceptions.DuplicateUserException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidCredentialsException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.AuditLog;
import application.domain.models.Buyer;
import application.domain.models.Cart;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.models.PhysicalProduct;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.in.AddCartItemUseCase;
import application.domain.ports.in.AdjustInventoryUseCase;
import application.domain.ports.in.ChangeBuyerCommercialStatusUseCase;
import application.domain.ports.in.ChangeProductStatusUseCase;
import application.domain.ports.in.ChangeUserStatusUseCase;
import application.domain.ports.in.CheckoutCartUseCase;
import application.domain.ports.in.ClearCartUseCase;
import application.domain.ports.in.ConfirmDeliveryUseCase;
import application.domain.ports.in.ConsultAuditLogUseCase;
import application.domain.ports.in.ConsultBuyerOrdersUseCase;
import application.domain.ports.in.ConsultBuyerUseCase;
import application.domain.ports.in.ConsultCartUseCase;
import application.domain.ports.in.ConsultCatalogUseCase;
import application.domain.ports.in.ConsultInventoryUseCase;
import application.domain.ports.in.ConsultOrderUseCase;
import application.domain.ports.in.ConsultSellerProductsUseCase;
import application.domain.ports.in.ConsultSellerUseCase;
import application.domain.ports.in.ConsultUserUseCase;
import application.domain.ports.in.ConsultWarehouseUseCase;
import application.domain.ports.in.DispatchOrderUseCase;
import application.domain.ports.in.LoginUseCase;
import application.domain.ports.in.OnboardSellerUseCase;
import application.domain.ports.in.ProcessOrderPaymentUseCase;
import application.domain.ports.in.PublishProductUseCase;
import application.domain.ports.in.RegisterBuyerUseCase;
import application.domain.ports.in.RegisterInventoryEntryUseCase;
import application.domain.ports.in.RegisterInventoryReturnUseCase;
import application.domain.ports.in.RegisterUserUseCase;
import application.domain.ports.in.RegisterWarehouseUseCase;
import application.domain.ports.in.ReleaseReservationUseCase;
import application.domain.ports.in.RemoveCartItemUseCase;
import application.domain.ports.in.ReserveInventoryUseCase;
import application.domain.ports.in.UpdateBuyerUseCase;
import application.domain.ports.in.UpdateProductUseCase;
import application.domain.valueobjects.CartStatus;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.InventoryItemCondition;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.StockQuantity;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.UserStatus;
import application.infrastructure.config.GlobalExceptionHandler;
import application.infrastructure.security.AuthenticatedUserResolver;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class RestControllersAndMappersTest {

    private final UserRestMapper userRestMapper = new UserRestMapper();
    private final BuyerRestMapper buyerRestMapper = new BuyerRestMapper();
    private final WarehouseRestMapper warehouseRestMapper = new WarehouseRestMapper();
    private final SellerRestMapper sellerRestMapper = new SellerRestMapper(warehouseRestMapper);
    private final ProductRestMapper productRestMapper = new ProductRestMapper();
    private final InventoryRestMapper inventoryRestMapper = new InventoryRestMapper();
    private final CartRestMapper cartRestMapper = new CartRestMapper();
    private final OrderRestMapper orderRestMapper = new OrderRestMapper();
    private final AuditRestMapper auditRestMapper = new AuditRestMapper();

    @Test
    void authUserBuyerSellerAndWarehouseControllersWorkEndToEnd() {
        LoginUseCase loginUseCase = mock(LoginUseCase.class);
        when(loginUseCase.login("admin", "pass")).thenReturn("JWT-TOKEN");
        AuthController authController = new AuthController(loginUseCase);
        var loginResp = authController.login(new LoginRequest("admin", "pass"));
        assertEquals(HttpStatus.OK, loginResp.getStatusCode());
        assertEquals("JWT-TOKEN", loginResp.getBody().data().token());

        AuthenticatedUserResolver resolver = mock(AuthenticatedUserResolver.class);
        User admin = new User();
        admin.setUsername("admin");
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        when(resolver.resolveUser()).thenReturn(admin);

        RegisterUserUseCase regUser = mock(RegisterUserUseCase.class);
        ChangeUserStatusUseCase chgUser = mock(ChangeUserStatusUseCase.class);
        ConsultUserUseCase conUser = mock(ConsultUserUseCase.class);
        when(regUser.register(eq(admin), any(User.class))).thenAnswer(inv -> inv.getArgument(1));
        UserController userController = new UserController(regUser, chgUser, conUser, userRestMapper, resolver);
        var createdUser = userController.register(new RegisterUserRequest(
                "DOC-1", "User One", "u1@test.com", "123", "Addr", "LOGISTICS_OPERATOR", "op1", "pass"));
        assertEquals(HttpStatus.CREATED, createdUser.getStatusCode());
        assertEquals("op1", createdUser.getBody().data().username());

        RegisterBuyerUseCase regBuyer = mock(RegisterBuyerUseCase.class);
        UpdateBuyerUseCase updBuyer = mock(UpdateBuyerUseCase.class);
        ChangeBuyerCommercialStatusUseCase chgBuyer = mock(ChangeBuyerCommercialStatusUseCase.class);
        ConsultBuyerUseCase conBuyer = mock(ConsultBuyerUseCase.class);
        when(regBuyer.register(any(Buyer.class), any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        BuyerController buyerController = new BuyerController(
                regBuyer, updBuyer, chgBuyer, conBuyer, buyerRestMapper, resolver);
        var createdBuyer = buyerController.register(new RegisterBuyerRequest(
                "DOC-B1", "Buyer One", "b1@test.com", "123", "Addr", "Main Addr", List.of("Alt"), "b1", "pass"));
        assertEquals(HttpStatus.CREATED, createdBuyer.getStatusCode());
        assertEquals("DOC-B1", createdBuyer.getBody().data().identification());

        OnboardSellerUseCase onboardSeller = mock(OnboardSellerUseCase.class);
        ConsultSellerUseCase conSeller = mock(ConsultSellerUseCase.class);
        ConsultSellerProductsUseCase conSellerProds = mock(ConsultSellerProductsUseCase.class);
        when(onboardSeller.onboard(eq(admin), any(Seller.class), any(Warehouse.class), any(User.class)))
                .thenAnswer(inv -> inv.getArgument(1));
        SellerController sellerController = new SellerController(
                onboardSeller, conSeller, conSellerProds, sellerRestMapper, productRestMapper, resolver);
        var onboarded = sellerController.onboard(new OnboardSellerRequest(
                "DOC-S1", "Seller One", "s1@test.com", "123", "Addr", "s1", "pass", "WH-1", "Main WH", "Bogota"));
        assertEquals(HttpStatus.CREATED, onboarded.getStatusCode());
        assertEquals("DOC-S1", onboarded.getBody().data().identification());

        RegisterWarehouseUseCase regWh = mock(RegisterWarehouseUseCase.class);
        ConsultWarehouseUseCase conWh = mock(ConsultWarehouseUseCase.class);
        when(regWh.register(eq(admin), any(Warehouse.class))).thenAnswer(inv -> inv.getArgument(1));
        WarehouseController whController = new WarehouseController(regWh, conWh, warehouseRestMapper, resolver);
        var createdWh = whController.register(new RegisterWarehouseRequest(
                "WH-MKT", "Central", "MARKETPLACE", null, "Medellin"));
        assertEquals(HttpStatus.CREATED, createdWh.getStatusCode());
        assertEquals("WH-MKT", createdWh.getBody().data().identifier());
    }

    @Test
    void productInventoryCartOrderAndAuditControllersAndExceptionHandlerWorkEndToEnd() {
        AuthenticatedUserResolver resolver = mock(AuthenticatedUserResolver.class);
        User actor = new User();
        actor.setUsername("actor");
        actor.setRole(UserRole.ADMIN);
        Buyer buyer = new Buyer();
        buyer.setIdentification(new DocumentId("DOC-BUYER"));
        when(resolver.resolveUser()).thenReturn(actor);
        when(resolver.resolveBuyer()).thenReturn(buyer);

        PublishProductUseCase pubProd = mock(PublishProductUseCase.class);
        UpdateProductUseCase updProd = mock(UpdateProductUseCase.class);
        ChangeProductStatusUseCase chgProd = mock(ChangeProductStatusUseCase.class);
        ConsultCatalogUseCase conCat = mock(ConsultCatalogUseCase.class);
        when(pubProd.publish(eq(actor), any())).thenAnswer(inv -> {
            PhysicalProduct p = inv.getArgument(1);
            p.setStatus(ProductStatus.PUBLISHED);
            return p;
        });
        ProductController prodController = new ProductController(
                pubProd, updProd, chgProd, conCat, productRestMapper, resolver);
        var pubResp = prodController.publish(new PublishProductRequest(
                "PROD-1", "Mouse", "Gaming", "PHYSICAL", BigDecimal.valueOf(50),
                "DOC-S1", 1L, "Tech", List.of(new ProductVariantDto("DPI", "16000"))));
        assertEquals(HttpStatus.CREATED, pubResp.getStatusCode());
        assertEquals("PROD-1", pubResp.getBody().data().identifier());

        RegisterInventoryEntryUseCase regEntry = mock(RegisterInventoryEntryUseCase.class);
        ReserveInventoryUseCase resInv = mock(ReserveInventoryUseCase.class);
        ReleaseReservationUseCase relInv = mock(ReleaseReservationUseCase.class);
        AdjustInventoryUseCase adjInv = mock(AdjustInventoryUseCase.class);
        RegisterInventoryReturnUseCase retInv = mock(RegisterInventoryReturnUseCase.class);
        ConsultInventoryUseCase conInv = mock(ConsultInventoryUseCase.class);
        when(regEntry.registerEntry(eq(actor), any(InventoryItem.class), eq(10))).thenAnswer(inv -> {
            InventoryItem item = inv.getArgument(1);
            item.setId(1L);
            item.setStock(StockQuantity.of(10));
            item.setCondition(InventoryItemCondition.AVAILABLE);
            return item;
        });
        when(retInv.registerReturn(eq(actor), any(InventoryItem.class), eq(2))).thenAnswer(inv -> {
            InventoryItem item = inv.getArgument(1);
            item.setId(1L);
            return InventoryMovement.of(item, InventoryMovementType.RETURN, 2, actor);
        });
        InventoryController invController = new InventoryController(
                regEntry, resInv, relInv, adjInv, retInv, conInv,
                inventoryRestMapper, productRestMapper, resolver);
        var entryResp = invController.registerEntry(
                new InventoryOperationRequest("PROD-1", "WH-1", 10, "AVAILABLE"));
        assertEquals(HttpStatus.CREATED, entryResp.getStatusCode());
        assertEquals(10, entryResp.getBody().data().stock());
        var returnResp = invController.registerReturn(
                new InventoryOperationRequest("PROD-1", "WH-1", 2, "AVAILABLE"));
        assertEquals("RETURN", returnResp.getBody().data().type());

        AddCartItemUseCase addCart = mock(AddCartItemUseCase.class);
        RemoveCartItemUseCase remCart = mock(RemoveCartItemUseCase.class);
        ClearCartUseCase clrCart = mock(ClearCartUseCase.class);
        ConsultCartUseCase conCart = mock(ConsultCartUseCase.class);
        when(addCart.addItem(eq(buyer), any())).thenAnswer(inv -> {
            Cart c = new Cart();
            c.setId(5L);
            c.setBuyer(buyer);
            c.setStatus(CartStatus.ACTIVE);
            c.setItems(List.of((application.domain.models.CartItem) inv.getArgument(1)));
            return c;
        });
        CartController cartController = new CartController(
                addCart, remCart, clrCart, conCart, cartRestMapper, resolver);
        var cartResp = cartController.addItem(new CartItemRequest("PROD-1", "Color", "Black", 2));
        assertEquals(HttpStatus.OK, cartResp.getStatusCode());
        assertEquals(1, cartResp.getBody().data().items().size());

        CheckoutCartUseCase checkout = mock(CheckoutCartUseCase.class);
        ProcessOrderPaymentUseCase pay = mock(ProcessOrderPaymentUseCase.class);
        DispatchOrderUseCase dispatch = mock(DispatchOrderUseCase.class);
        ConfirmDeliveryUseCase deliver = mock(ConfirmDeliveryUseCase.class);
        ConsultOrderUseCase conOrder = mock(ConsultOrderUseCase.class);
        ConsultBuyerOrdersUseCase conBuyerOrders = mock(ConsultBuyerOrdersUseCase.class);
        when(checkout.checkout(buyer)).thenAnswer(inv -> {
            Order o = new Order();
            o.setIdentifier("ORD-1");
            o.setBuyer(buyer);
            o.setStatus(OrderStatus.PENDING_PAYMENT);
            o.setTotal(Money.of(BigDecimal.valueOf(100)));
            return o;
        });
        OrderController orderController = new OrderController(
                checkout, pay, dispatch, deliver, conOrder, conBuyerOrders,
                orderRestMapper, buyerRestMapper, resolver);
        var orderResp = orderController.checkout();
        assertEquals(HttpStatus.CREATED, orderResp.getStatusCode());
        assertEquals("ORD-1", orderResp.getBody().data().identifier());

        ConsultAuditLogUseCase conAudit = mock(ConsultAuditLogUseCase.class);
        AuditLog log = new AuditLog();
        log.setAuditId("AUD-1");
        log.setOperationType(OperationType.CART_CHECKOUT);
        log.setOperationDate(LocalDateTime.now());
        log.setSeverity(AuditSeverity.INFO);
        log.setDetails(Map.of("k", "v"));
        when(conAudit.consult(eq(actor), any())).thenReturn(List.of(log));
        AuditController auditController = new AuditController(conAudit, auditRestMapper, resolver);
        var auditResp = auditController.consult("Order", "ORD-1");
        assertEquals(1, auditResp.getBody().data().size());

        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        assertEquals(HttpStatus.UNAUTHORIZED,
                handler.handleInvalidCredentials(new InvalidCredentialsException()).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN,
                handler.handleUnauthorizedOperation(new UnauthorizedOperationException("forbidden")).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND,
                handler.handleEntityNotFound(new EntityNotFoundException("Order not found")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT,
                handler.handleDuplicateUser(new DuplicateUserException("dup")).getStatusCode());
        assertNotNull(new ChangeStatusRequest("ACTIVE").status());
    }
}
