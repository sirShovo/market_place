# Input Ports

## Introduction

Input Ports are the **use-case contracts** the outer layers (REST controllers in
Phase 6) invoke. They apply the Dependency Inversion Principle: the web layer depends
on these interfaces, not on the concrete domain services.

Conventions:

* **One interface per use case**, named `<Verb><Noun>UseCase`.
* **A single method**, named with a business verb (`register`, `checkout`, `reserve`,
  `dispatch`, `consult`, ...).
* Parameters and return values are **Domain Models / Value Objects**, never DTOs,
  persistence entities or bare identifiers.
* Each interface is implemented by **exactly one** domain service (same base name +
  `Service`).

Location: `application.domain.ports.in`.

---

## Parameter Rule

Incorrect:

```java
interface CheckoutCartUseCase { Order checkout(String buyerId); }
```

Correct:

```java
interface CheckoutCartUseCase { Order checkout(Buyer buyer); }
```

The REST layer (Phase 6) maps a Request DTO into the Domain Model *before* calling the
Input Port; it maps the returned Domain Model into a Response DTO *after*. DTOs never
cross into the domain.

---

## Catalogue by subdomain

### User

```java
interface RegisterUserUseCase     { User register(User requester, User newUser); }
interface ChangeUserStatusUseCase { User changeStatus(User requester, User target); }
interface ConsultUserUseCase      { User consult(User requester, User probe); }
```

### Buyer

```java
interface RegisterBuyerUseCase { Buyer register(Buyer buyer); }             // self-service
interface UpdateBuyerUseCase   { Buyer update(User requester, Buyer buyer); }
interface ConsultBuyerUseCase  { Buyer consult(User requester, Buyer probe); }
```

### Seller

```java
interface OnboardSellerUseCase { Seller onboard(User requester, Seller seller, Warehouse firstWarehouse); }
interface ConsultSellerUseCase { Seller consult(User requester, Seller probe); }
```

### Warehouse

```java
interface RegisterWarehouseUseCase { Warehouse register(User requester, Warehouse warehouse); }
interface ConsultWarehouseUseCase  { Warehouse consult(User requester, Warehouse probe); }
```

### Catalog

```java
interface PublishProductUseCase      { Product publish(User requester, Product product); }
interface UpdateProductUseCase       { Product update(User requester, Product product); }
interface ChangeProductStatusUseCase { Product changeStatus(User requester, Product product); }
interface ConsultCatalogUseCase      { List<Product> consultPublished(); }   // public
```

### Inventory

```java
interface RegisterInventoryEntryUseCase { InventoryItem registerEntry(User requester, InventoryItem probe, int quantity); }
interface ReserveInventoryUseCase       { InventoryMovement reserve(User requester, InventoryItem probe, int quantity); }
interface ReleaseReservationUseCase     { InventoryMovement release(User requester, InventoryItem probe, int quantity); }
interface AdjustInventoryUseCase        { InventoryMovement adjust(User requester, InventoryItem probe, int newQuantity); }
interface ConsultInventoryUseCase       { List<InventoryItem> consult(User requester, Product product); }
```

### Cart

```java
interface AddCartItemUseCase    { Cart addItem(Buyer buyer, CartItem item); }
interface RemoveCartItemUseCase { Cart removeItem(Buyer buyer, CartItem item); }
interface ClearCartUseCase      { Cart clear(Buyer buyer); }
interface ConsultCartUseCase    { Cart consult(Buyer buyer); }
```

### Order

```java
interface CheckoutCartUseCase        { Order checkout(Buyer buyer); }
interface ProcessOrderPaymentUseCase { Order pay(Buyer buyer, Order order); }
interface DispatchOrderUseCase       { Order dispatch(User requester, Order order); }
interface ConfirmDeliveryUseCase     { Order confirmDelivery(User requester, Order order); }
interface ConsultOrderUseCase        { Order consult(User requester, Order probe); }
```

### Operation & Audit

```java
interface ConsultAuditLogUseCase { List<AuditLog> consult(User requester, AuditableEntity entity); }
```

---

## `requester` vs `buyer` in the signatures

* Services whose actor is a **system user** (admin / seller / logistics operator)
  take `User requester` and validate role + status through the `authorization/`
  services.
* Services whose actor is a **buyer acting on its own commercial data** (cart, and the
  buyer-driven order steps) take `Buyer buyer` and validate commercial status through
  `ValidateBuyerCanPurchaseService`.

---

## Internal services without an Input Port

The `authorization/` and `operation/` services (except `ConsultAuditLogService`) are
**internal collaborators** composed by the use-case services above. They are not
exposed as Input Ports and have no REST entry point. See
[authorization-services.md](services/authorization-services.md) and
[operation-audit-services.md](services/operation-audit-services.md).
