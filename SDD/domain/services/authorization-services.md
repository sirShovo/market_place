# Authorization Services

## Introduction

This document defines the services belonging to the **Authorization** subdomain of
NexusMarket. They enforce spec **RG01** (every operation is performed by an
authenticated user), **RG03** (no participant manages information outside its role)
and the **responsibility matrix §12**.

These services are **internal collaborators**: they are composed by the use-case
services of the other subdomains and are **not exposed as Input Ports** — there is no
REST entry point for them. They contain no persistence; every check is evaluated from
the Domain Models supplied by the caller (spec principle: business rules that can be
decided from Domain Models stay inside the domain).

Every service throws `UnauthorizedOperationException` on failure and returns silently
on success.

Location: `application.domain.services.authorization`.

---

## Design Principles

* **Domain Models only** — each method receives the requester and, where relevant, the
  target entity, as Domain Models.
* **No output ports** — these services never call a repository. If an authorization
  decision needed data not present in the supplied models, the *calling* service would
  load it first and pass it in.
* **Composable** — a use-case service typically calls two or three of these in
  sequence (status, then role, then ownership).
* **Wiring** — plain `@Service` beans, no constructor dependencies.

---

## 1. ValidateRoleAuthorizationService

```java
void execute(User requester, UserRole... allowedRoles)
```

Passes only if `requester` is non-null, has a `role`, and that role equals one of
`allowedRoles`. Used by every command service to encode the responsibility matrix
(e.g. `PublishProductService` calls it with `SELLER`; `OnboardSellerService` with
`ADMIN`).

---

## 2. ValidateUserStatusService

```java
void execute(User requester)
```

Passes only if `requester` is non-null and `status == UserStatus.ACTIVE`
(spec RG01). Called first by every service that takes a `User requester`.

---

## 3. ValidateBuyerOwnershipService

```java
void execute(User requester, Buyer target)
```

* `ADMIN` / `SUPERVISOR` → passes for any buyer.
* `BUYER` → passes only if `requester.identification` equals `target.identification`.
* Any other case → `UnauthorizedOperationException`.

Used by `UpdateBuyerService` and `ConsultBuyerService` (spec Domain 2, RG03).

---

## 4. ValidateProductOwnershipService

```java
void execute(User requester, Product product)
```

* `ADMIN` → passes for any product.
* `SELLER` → passes only if `product.seller.identification` equals
  `requester.identification`.
* Any other case → `UnauthorizedOperationException`.

Used by `UpdateProductService` and `ChangeProductStatusService`, evaluated against the
**stored** product (spec §12, RG03).

---

## 5. ValidateOrderAccessService

```java
void execute(User requester, Order order)
```

* `ADMIN` / `SUPERVISOR` / `LOGISTICS_OPERATOR` → passes for any order.
* `BUYER` → passes only if `order.buyer.identification` equals
  `requester.identification`.
* Any other case → `UnauthorizedOperationException`.

Used by `ConsultOrderService` (spec RG03).

---

## 6. ValidateBuyerCanPurchaseService

```java
void execute(Buyer buyer)
```

Passes only if `buyer` is non-null and
`commercialStatus == BuyerCommercialStatus.ACTIVE` (spec Domain 2: commercial status
governs the ability to purchase). Used by `AddCartItemService`,
`CheckoutCartService` and `ProcessOrderPaymentService`.

---

## Composition Example

```text
UpdateBuyerService.update(requester, buyer)
        │
        ├── ValidateUserStatusService.execute(requester)          (RG01)
        ├── ValidateBuyerOwnershipService.execute(requester, buyer) (RG03)
        └── ... proceed ...
```

```text
PublishProductService.publish(requester, product)
        │
        ├── ValidateUserStatusService.execute(requester)          (RG01)
        ├── ValidateRoleAuthorizationService.execute(requester, SELLER)  (§12)
        └── ... proceed ...
```
