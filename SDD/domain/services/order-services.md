# Order Services

## Introduction

This document defines the services belonging to the **Order Management** subdomain of
NexusMarket. These services own the lifecycle of an `Order` — the formal commercial
commitment described in spec **Domain 7** — from cart conversion through payment,
dispatch and delivery.

Every state-changing action in the order lifecycle generates an `Operation` and a
corresponding `AuditLog` record (spec traceability requirements). A finalized
(`DELIVERED`) order is immutable (spec §11).

The services described here define the conceptual behavior of the subdomain. REST
contracts, persistence mappings and infrastructure concerns are documented separately
(phases 5–6).

Location: `application.domain.services.order`.

---

## Domain Model Context

```text
Order
 ├── identifier   : String            (generated at checkout)
 ├── buyer        : Buyer
 ├── items        : List<OrderItem>
 ├── total        : Money              (sum of item subtotals)
 ├── status       : OrderStatus
 └── createdAt    : LocalDateTime

OrderItem
 ├── product      : Product
 ├── variant      : ProductVariant?
 ├── quantity     : int
 ├── unitPrice    : Money              (captured at checkout time)
 ├── warehouse    : Warehouse?         (set at checkout for physical lines)
 └── subtotal     : Money  (derived)   (unitPrice × quantity)
```

All relationships use Domain Models. The model never replaces them with primitive
identifiers such as `String buyerId` or `String productId`.

---

## Service Design Principles

### Domain Model parameters

Every order service and Input Port receives Domain Models or Value Objects. They never
receive primitive identifiers, REST request DTOs or persistence entities.

Incorrect:

```java
Order pay(String orderId, String buyerId);
```

Correct:

```java
Order pay(Buyer buyer, Order order);
```

### External information

Order services use information already present in the Domain Models first. When a rule
needs something else, the service uses an Output Port. Services never touch MySQL,
MongoDB, JPA, SQL, HTTP or a payment SDK directly.

```text
Order Service
      │
      ▼
Output Port  (OrderRepositoryPort / PaymentGatewayPort / NotificationPort)
      │
      ▼
Output Adapter
      │
      ▼
External Resource
```

### Wiring

Each service is annotated `@Service` with constructor injection
(`@RequiredArgsConstructor`). Each is unit-testable by direct instantiation with
test-double ports; no Spring context is required.

---

## Order Lifecycle

`OrderStatus` and the allowed transitions are enforced by `Order.transitionTo(...)`.

```text
        checkout
CART ─────────────► PENDING_PAYMENT
                          │
                          │ payment approved
                          ▼
                        PAID ──────────────► DELIVERED
                          │  (digital-only order)
                          │ dispatch
                          ▼
                     DISPATCHED
                          │ confirm delivery
                          ▼
                      DELIVERED   (finalized, immutable — spec §11)
```

* A rejected payment leaves the order in `PENDING_PAYMENT`; the buyer may retry.
* Any transition attempted from `DELIVERED`, or any `addItem` / `recalculateTotal` on
  a `DELIVERED` order, raises `FinalizedOrderModificationException`.
* Any transition not in the table above raises `InvalidStatusTransitionException`.

---

## 1. Checkout Cart

### Description

Converts the buyer's active cart into an `Order`, moving it from `CART` to
`PENDING_PAYMENT`. Each cart line becomes an `OrderItem` whose `unitPrice` is captured
from the product at checkout time, so later price changes do not affect the order.
**Every physical line reserves inventory before the order is persisted** — checkout is
the moment stock actually leaves the "available" pool (spec Domain 6, §11); digital
lines carry no inventory and are skipped.

Input Port: `CheckoutCartUseCase` — `Order checkout(Buyer buyer)`.

### Input

```text
Buyer
```

### Authorization

`ValidateBuyerCanPurchaseService` — the buyer's `commercialStatus` must be `ACTIVE`
(spec Domain 2).

### Domain Validations

* An active cart must exist for the buyer (`EntityNotFoundException`).
* The cart must contain at least one line (`DomainException`).
* For each **physical** line, `ReserveStockForOrderItemService` must find a warehouse
  with `AVAILABLE` stock ≥ the requested quantity, otherwise
  `InvalidReservationException` aborts the whole checkout (spec §11).

### Effect / State Change

* A new `Order` is created with a generated `identifier` and `createdAt = now`.
* One `OrderItem` per cart line: `product`, `variant`, `quantity`,
  `unitPrice = product.price`.
* For each **physical** line: `ReserveStockForOrderItemService` picks a warehouse with
  enough `AVAILABLE` stock, decrements it, appends a `RESERVATION` movement, and sets
  `orderItem.warehouse` to the chosen warehouse. Digital lines leave `warehouse = null`.
* `Order.total` is recomputed as the sum of subtotals.
* `CART → PENDING_PAYMENT`.
* The cart is marked `CONVERTED`.

> **Not transactional at this layer.** If reservation fails for a later line, earlier
> reservations in the same checkout are **not** rolled back here — Phase 5 wraps the
> whole checkout in a transaction at the adapter boundary.

### Persistence

```text
ReserveStockForOrderItemService.reserve(item)   (per physical line — see inventory-services.md)
OrderRepositoryPort.save(order)
CartRepositoryPort.update(cart)
```

### Operation and Audit

Operation type: `CART_CHECKOUT`, severity `INFO`. Each physical-line reservation also
registers its own `INVENTORY_RESERVATION` operation (see
[inventory-services.md](inventory-services.md)) with `performedBy = null`, since the
action is a system side effect of the buyer's own checkout rather than staff-driven.

```text
Cart
  │
  ▼
CheckoutCartService
  ├── for each physical line: ReserveStockForOrderItemService.reserve
  │        └── Operation (INVENTORY_RESERVATION) ──► AuditLog
  ├── persist Order
  ├── mark Cart CONVERTED
  └── Operation (CART_CHECKOUT)
         │
         ▼
      AuditLog   details = { order, lines, total }
```

---

## 2. Process Order Payment

### Description

Processes the payment of a `PENDING_PAYMENT` order through `PaymentGatewayPort`. The
gateway's **simulation** (probabilistic approval / rejection that lets the buyer
retry) lives in the adapter; the domain only sees a `PaymentResult`.

Input Port: `ProcessOrderPaymentUseCase` — `Order pay(Buyer buyer, Order order)`.

### Input

```text
Buyer
Order
```

### Authorization

* `ValidateBuyerCanPurchaseService` — the buyer must be commercially `ACTIVE`.
* `ValidateBuyerOwnsOrderService` — the order's `buyer.identification` must match the
  paying buyer's; otherwise `UnauthorizedOperationException` (spec RG03). A buyer
  cannot pay for, or trigger notifications/audit entries against, another buyer's
  order.

### Domain Validations

* The order must exist (`EntityNotFoundException`).
* The order must belong to the paying buyer (see Authorization above).
* The order must be in `PENDING_PAYMENT`; otherwise the transition to `PAID` raises
  `InvalidStatusTransitionException`.

### Effect / State Change

| Gateway result | Effect |
| -------------- | ------ |
| `REJECTED` | Order stays `PENDING_PAYMENT`. `PaymentRejectedException` is thrown so the buyer can retry. |
| `APPROVED` | `PENDING_PAYMENT → PAID`. If the order contains **no** physical products, `PAID → DELIVERED` immediately (digital delivery). |

On approval, a confirmation `Notification` (channel `EMAIL`) is dispatched to the
buyer through `NotificationPort`.

### Persistence

```text
OrderRepositoryPort.findByIdentifier(order)
OrderRepositoryPort.update(order)         (approval path only)
```

### Operation and Audit

Operation type: `ORDER_PAYMENT`.

| Path | Severity | details |
| ---- | -------- | ------- |
| Rejected | `ERROR` | `{ order, result: "REJECTED" }` |
| Approved | `INFO` | `{ order, result: "APPROVED", status }` |

```text
Order (PENDING_PAYMENT)
      │
      ▼
PaymentGatewayPort.process(order)
      │
      ├── REJECTED ──► AuditLog(ERROR) ──► throw PaymentRejectedException
      │
      └── APPROVED ──► PAID ──► (digital? → DELIVERED)
                         │
                         ├── NotificationPort.send(confirmation)
                         └── Operation(ORDER_PAYMENT) ──► AuditLog(INFO)
```

---

## 3. Dispatch Order

### Description

A `LOGISTICS_OPERATOR` dispatches a paid order that contains physical products,
moving it from `PAID` to `DISPATCHED` and recording a `SALE_EXIT` inventory movement
for every physical line against its assigned fulfillment warehouse (spec Domain 6,
Domain 7, responsibility matrix §12). Stock was already decremented at checkout when
the reservation was made, so `SALE_EXIT` records the physical warehouse exit for
traceability without deducting stock a second time.

Input Port: `DispatchOrderUseCase` — `Order dispatch(User requester, Order order)`.

### Input

```text
User    (requester)
Order
```

### Authorization

* `ValidateUserStatusService` — requester is `ACTIVE` (spec RG01).
* `ValidateRoleAuthorizationService` — role `LOGISTICS_OPERATOR`.

### Domain Validations

* The order must exist (`EntityNotFoundException`).
* The order must contain at least one physical product (`DomainException`).
* `PAID → DISPATCHED` must be a valid transition (`InvalidStatusTransitionException`).
* Each physical `OrderItem` must reference its fulfillment `warehouse` (`DomainException`),
  and the corresponding `InventoryItem` for `(product, warehouse)` must exist
  (`EntityNotFoundException`).

### Effect / State Change

* `PAID → DISPATCHED`.
* For each physical `OrderItem`, an `InventoryMovement` of type `SALE_EXIT` is saved
  for the `(product, warehouse)` `InventoryItem` with `performedBy = requester`.

### Persistence

```text
OrderRepositoryPort.findByIdentifier(order)
InventoryRepositoryPort.findByProductAndWarehouse(probe)  (per physical line)
InventoryRepositoryPort.saveMovement(SALE_EXIT)           (per physical line)
OrderRepositoryPort.update(order)
```

### Operation and Audit

Operation type: `ORDER_DISPATCH`, severity `INFO`, details `{ order }`.

---

## 4. Confirm Delivery

### Description

A `LOGISTICS_OPERATOR` confirms delivery of a dispatched order, moving it from
`DISPATCHED` to `DELIVERED`. The order becomes finalized and immutable (spec §11).

Input Port: `ConfirmDeliveryUseCase` — `Order confirmDelivery(User requester, Order order)`.

### Input

```text
User    (requester)
Order
```

### Authorization

* `ValidateUserStatusService` — requester is `ACTIVE`.
* `ValidateRoleAuthorizationService` — role `LOGISTICS_OPERATOR`.

### Domain Validations

* The order must exist (`EntityNotFoundException`).
* `DISPATCHED → DELIVERED` must be a valid transition
  (`InvalidStatusTransitionException`).

### Persistence

```text
OrderRepositoryPort.findByIdentifier(order)
OrderRepositoryPort.update(order)
```

### Operation and Audit

Operation type: `ORDER_DELIVERY`, severity `INFO`, details `{ order }`.

---

## 5. Consult Order

### Description

Returns an order represented by the `Order` Domain Model. The result never exposes
persistence entities.

Input Port: `ConsultOrderUseCase` — `Order consult(User requester, Order probe)`.

### Input

```text
User    (requester)
Order   (probe — carries the identifier)
```

### Authorization

* `ValidateUserStatusService` — requester is `ACTIVE`.
* `ValidateOrderAccessService` — `ADMIN`, `SUPERVISOR` and `LOGISTICS_OPERATOR` may
  read any order; a `BUYER` may read only orders whose `buyer.identification` matches
  its own (spec RG03).

### Domain Validations

* The order must exist (`EntityNotFoundException`).

### Operation and Audit

Not an audited operation (read-only).

---

## 6. Consult Buyer Orders

### Description

Returns all orders placed by a given buyer. A `BUYER` may only list its own orders;
`ADMIN` and `SUPERVISOR` may list any buyer's orders (spec RG03).

Input Port: `ConsultBuyerOrdersUseCase` —
`List<Order> consultByBuyer(User requester, Buyer buyer)`.

### Input

```text
User    (requester)
Buyer   (probe — carries the buyer's identification)
```

### Authorization

* `ValidateUserStatusService` — requester is `ACTIVE`.
* `ValidateBuyerOwnershipService` — a `BUYER` may only list its own orders;
  `ADMIN` / `SUPERVISOR` may list any buyer's.

### Persistence

```text
OrderRepositoryPort.findByBuyer(buyer)
```

### Operation and Audit

Not an audited operation (read-only).

---

## Output Ports

The Order subdomain requires:

```java
interface OrderRepositoryPort {
    Order save(Order order);
    Optional<Order> findByIdentifier(Order order);
    List<Order> findByBuyer(Buyer buyer);
    void update(Order order);
}

interface CartRepositoryPort {
    Optional<Cart> findActiveByBuyer(Buyer buyer);
    void update(Cart cart);
    // ...
}

interface InventoryRepositoryPort {
    // consumed via ReserveStockForOrderItemService at checkout and directly in
    // DispatchOrderService to record per-line SALE_EXIT movements —
    // see inventory-services.md
}

interface PaymentGatewayPort {
    PaymentResult process(Order order);   // simulation + retry lives in the adapter
}

interface NotificationPort {
    void send(Notification notification);
}

interface OperationRepositoryPort  { Operation save(Operation operation); /* ... */ }
interface AuditLogRepositoryPort   { AuditLog  save(AuditLog auditLog);   /* ... */ }
```

All interfaces live in `application.domain.ports.out`. See
[Output Ports](../Output%20Ports.md) for the full contracts.

---

## Input Ports

```java
interface CheckoutCartUseCase        { Order checkout(Buyer buyer); }
interface ProcessOrderPaymentUseCase { Order pay(Buyer buyer, Order order); }
interface DispatchOrderUseCase       { Order dispatch(User requester, Order order); }
interface ConfirmDeliveryUseCase     { Order confirmDelivery(User requester, Order order); }
interface ConsultOrderUseCase        { Order consult(User requester, Order probe); }
interface ConsultBuyerOrdersUseCase  { List<Order> consultByBuyer(User requester, Buyer buyer); }
```

All interfaces live in `application.domain.ports.in`. Each is implemented by exactly
one service in this subdomain.

---

## Checkout Flow

```text
HTTP Request  (Phase 6)
     │
     ▼
Request DTO ──► Request Mapper ──► Buyer (Domain Model)
     │
     ▼
CheckoutCartUseCase
     │
     ▼
CheckoutCartService
     ├── ValidateBuyerCanPurchaseService
     ├── CartRepositoryPort.findActiveByBuyer
     ├── build Order + OrderItems (capture unitPrice)
     ├── for each physical line: ReserveStockForOrderItemService.reserve
     │        ├── InventoryRepositoryPort.findByProduct / update / saveMovement
     │        └── RegisterOperationAndAuditService  (INVENTORY_RESERVATION)
     ├── Order.transitionTo(PENDING_PAYMENT)
     ├── OrderRepositoryPort.save
     ├── CartRepositoryPort.update  (CONVERTED)
     └── RegisterOperationAndAuditService  (CART_CHECKOUT)
```

## Payment Flow

```text
Order (PENDING_PAYMENT)
     │
     ▼
ProcessOrderPaymentUseCase
     │
     ▼
ProcessOrderPaymentService
     ├── ValidateBuyerCanPurchaseService
     ├── OrderRepositoryPort.findByIdentifier
     ├── ValidateBuyerOwnsOrderService
     ├── PaymentGatewayPort.process
     │        ├── REJECTED → AuditLog(ERROR) → PaymentRejectedException
     │        └── APPROVED
     ├── Order.transitionTo(PAID)  [→ DELIVERED if digital-only]
     ├── OrderRepositoryPort.update
     ├── NotificationPort.send
     └── RegisterOperationAndAuditService  (ORDER_PAYMENT)
```
