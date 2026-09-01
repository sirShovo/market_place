# Cart Services

## Introduction

This document defines the services belonging to the **Cart Management** subdomain of
NexusMarket (spec **Domain 7 — state 1, Cart**). The cart is a *provisional* selection
of products; it becomes an `Order` only at checkout, which is owned by the
[Order Services](order-services.md).

Each buyer has a **single active `Cart`**. Cart operations are low-significance and
**not audited**.

The services described here define the conceptual behavior of the subdomain. REST
contracts, persistence mappings and infrastructure concerns are documented separately
(phases 5–6).

Location: `application.domain.services.cart`.

---

## Domain Model Context

```text
Cart (implements AuditableEntity)
 ├── id     : Long
 ├── buyer  : Buyer
 ├── items  : List<CartItem>
 └── status : CartStatus   (ACTIVE / CONVERTED / ABANDONED)

CartItem
 ├── product  : Product
 ├── variant  : ProductVariant?
 └── quantity : int
```

There is at most one `ACTIVE` cart per buyer at any time, retrieved via
`CartRepositoryPort.findActiveByBuyer`.

---

## Service Design Principles

### Domain Model parameters

Services and Input Ports receive Domain Models, never primitive identifiers, REST
request DTOs or persistence entities.

Incorrect:

```java
Cart addItem(String buyerId, String productId, int qty);
```

Correct:

```java
Cart addItem(Buyer buyer, CartItem item);
```

### Authorization

`ValidateBuyerCanPurchaseService` — the buyer's `commercialStatus` must be `ACTIVE`
(spec Domain 2) for `AddCartItemService`. The remaining operations act on an already
existing cart owned by the buyer.

### External information

Services reach the outside only through `CartRepositoryPort`. They never touch JPA,
SQL or HTTP directly.

### Wiring

`@Service` + constructor injection. Unit-testable by direct instantiation with
test-double ports.

---

## 1. Add Cart Item

### Description

Adds a line to the buyer's active cart, creating the cart (`ACTIVE`) on first use.

Input Port: `AddCartItemUseCase` — `Cart addItem(Buyer buyer, CartItem item)`.

### Input

```text
Buyer
CartItem   (carries product, variant?, quantity)
```

### Authorization

`ValidateBuyerCanPurchaseService` — buyer commercially `ACTIVE`.

### Domain Validations

* `item.product` must be `PUBLISHED` (`DomainException`).
* `item.quantity > 0` (`DomainException`).

### Effect / State Change

* If no active cart exists, one is created (`ACTIVE`) and saved.
* The `CartItem` is appended to `cart.items`.

### Persistence

```text
CartRepositoryPort.findActiveByBuyer(buyer)
CartRepositoryPort.save(freshCart)    (first use only)
CartRepositoryPort.update(cart)
```

### Operation and Audit

Not audited.

---

## 2. Remove Cart Item

### Description

Removes the line matching the given product and variant from the buyer's active cart.

Input Port: `RemoveCartItemUseCase` — `Cart removeItem(Buyer buyer, CartItem item)`.

### Input

```text
Buyer
CartItem   (identifies the line by product identifier + variant)
```

### Domain Validations

* An active cart must exist (`EntityNotFoundException`).

### Effect / State Change

* Any line with the same product identifier and equal variant is removed.

### Persistence

```text
CartRepositoryPort.findActiveByBuyer(buyer)
CartRepositoryPort.update(cart)
```

### Operation and Audit

Not audited.

---

## 3. Clear Cart

### Description

Empties the buyer's active cart.

Input Port: `ClearCartUseCase` — `Cart clear(Buyer buyer)`.

### Input

```text
Buyer
```

### Domain Validations

* An active cart must exist (`EntityNotFoundException`).

### Effect / State Change

* `cart.items` is cleared.

### Persistence

```text
CartRepositoryPort.findActiveByBuyer(buyer)
CartRepositoryPort.update(cart)
```

### Operation and Audit

Not audited.

---

## 4. Consult Cart

### Description

Returns the buyer's active cart, or an empty **transient** cart (`ACTIVE`, not
persisted) when none exists yet.

Input Port: `ConsultCartUseCase` — `Cart consult(Buyer buyer)`.

### Input

```text
Buyer
```

### Persistence

```text
CartRepositoryPort.findActiveByBuyer(buyer)
```

### Operation and Audit

Not an audited operation (read-only).

---

## Output Ports

```java
interface CartRepositoryPort {
    Cart save(Cart cart);
    Optional<Cart> findActiveByBuyer(Buyer buyer);
    Optional<Cart> findById(Cart cart);
    void update(Cart cart);
}
```

See [Output Ports](../Output%20Ports.md) for the full contracts.

---

## Input Ports

```java
interface AddCartItemUseCase    { Cart addItem(Buyer buyer, CartItem item); }
interface RemoveCartItemUseCase { Cart removeItem(Buyer buyer, CartItem item); }
interface ClearCartUseCase      { Cart clear(Buyer buyer); }
interface ConsultCartUseCase    { Cart consult(Buyer buyer); }
```

---

## Cart → Order Handover

```text
Cart (ACTIVE)
  │  buyer proceeds to checkout
  ▼
CheckoutCartService   (order subdomain)
  ├── reads the active cart
  ├── builds Order + OrderItems (captures unitPrice)
  └── marks the cart CONVERTED
```
