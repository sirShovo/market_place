# Cart Services

Subdomain: `application.domain.services.cart`. Covers spec **Domain 7 — state 1
(Cart)**. Each buyer has a single active `Cart`. Cart operations are provisional and
**not audited**.

`ValidateBuyerCanPurchaseService` — the buyer must be commercially `ACTIVE`
(spec Domain 2).

---

## AddCartItemService — `AddCartItemUseCase`

Adds a line to the buyer's active cart, creating the cart on first use.

* **Validations:** buyer commercially `ACTIVE`; product must be `PUBLISHED`; quantity
  > 0 (`DomainException`).
* **Ports:** `CartRepositoryPort`.

## RemoveCartItemService — `RemoveCartItemUseCase`

Removes the line matching the given product + variant. Requires an active cart
(`EntityNotFoundException`).

## ClearCartService — `ClearCartUseCase`

Empties the active cart.

## ConsultCartService — `ConsultCartUseCase`

Returns the active cart, or an empty transient cart when none exists yet.

---

**Ports (all):** `CartRepositoryPort` (`save`, `findActiveByBuyer`, `findById`,
`update`).
