# Input Ports

## Introduction

Input Ports are the use-case contracts the outer layers (REST controllers in Phase 6)
invoke. They apply the Dependency Inversion Principle: the web layer depends on these
interfaces, not on the concrete domain services.

* One interface per use case.
* A single method, named with a business verb.
* Parameters and return values are **Domain Models / Value Objects**, never DTOs or
  bare identifiers.
* Each interface is implemented by exactly one domain service (same base name +
  `Service`).

Location: `application.domain.ports.in`.

---

# Catalogue

| Input Port | Method | Implemented by |
| ---------- | ------ | -------------- |
| `RegisterUserUseCase` | `User register(User requester, User newUser)` | `RegisterUserService` |
| `ChangeUserStatusUseCase` | `User changeStatus(User requester, User target)` | `ChangeUserStatusService` |
| `ConsultUserUseCase` | `User consult(User requester, User probe)` | `ConsultUserService` |
| `RegisterBuyerUseCase` | `Buyer register(Buyer buyer)` | `RegisterBuyerService` |
| `UpdateBuyerUseCase` | `Buyer update(User requester, Buyer buyer)` | `UpdateBuyerService` |
| `ConsultBuyerUseCase` | `Buyer consult(User requester, Buyer probe)` | `ConsultBuyerService` |
| `OnboardSellerUseCase` | `Seller onboard(User requester, Seller seller, Warehouse firstWarehouse)` | `OnboardSellerService` |
| `ConsultSellerUseCase` | `Seller consult(User requester, Seller probe)` | `ConsultSellerService` |
| `RegisterWarehouseUseCase` | `Warehouse register(User requester, Warehouse warehouse)` | `RegisterWarehouseService` |
| `ConsultWarehouseUseCase` | `Warehouse consult(User requester, Warehouse probe)` | `ConsultWarehouseService` |
| `PublishProductUseCase` | `Product publish(User requester, Product product)` | `PublishProductService` |
| `UpdateProductUseCase` | `Product update(User requester, Product product)` | `UpdateProductService` |
| `ChangeProductStatusUseCase` | `Product changeStatus(User requester, Product product)` | `ChangeProductStatusService` |
| `ConsultCatalogUseCase` | `List<Product> consultPublished()` | `ConsultCatalogService` |
| `RegisterInventoryEntryUseCase` | `InventoryItem registerEntry(User requester, InventoryItem item, int quantity)` | `RegisterInventoryEntryService` |
| `ReserveInventoryUseCase` | `InventoryMovement reserve(User requester, InventoryItem probe, int quantity)` | `ReserveInventoryService` |
| `ReleaseReservationUseCase` | `InventoryMovement release(User requester, InventoryItem probe, int quantity)` | `ReleaseReservationService` |
| `AdjustInventoryUseCase` | `InventoryMovement adjust(User requester, InventoryItem probe, int newQuantity)` | `AdjustInventoryService` |
| `ConsultInventoryUseCase` | `List<InventoryItem> consult(User requester, Product product)` | `ConsultInventoryService` |
| `AddCartItemUseCase` | `Cart addItem(Buyer buyer, CartItem item)` | `AddCartItemService` |
| `RemoveCartItemUseCase` | `Cart removeItem(Buyer buyer, CartItem item)` | `RemoveCartItemService` |
| `ClearCartUseCase` | `Cart clear(Buyer buyer)` | `ClearCartService` |
| `ConsultCartUseCase` | `Cart consult(Buyer buyer)` | `ConsultCartService` |
| `CheckoutCartUseCase` | `Order checkout(Buyer buyer)` | `CheckoutCartService` |
| `ProcessOrderPaymentUseCase` | `Order pay(Buyer buyer, Order order)` | `ProcessOrderPaymentService` |
| `DispatchOrderUseCase` | `Order dispatch(User requester, Order order)` | `DispatchOrderService` |
| `ConfirmDeliveryUseCase` | `Order confirmDelivery(User requester, Order order)` | `ConfirmDeliveryService` |
| `ConsultOrderUseCase` | `Order consult(User requester, Order probe)` | `ConsultOrderService` |
| `ConsultAuditLogUseCase` | `List<AuditLog> consult(User requester, AuditableEntity entity)` | `ConsultAuditLogService` |

---

# Authorization and Operation subdomains

`authorization/` and `operation/` services are **internal collaborators** composed by
the use-case services above. They are not exposed as Input Ports and have no REST
entry point.
