# Order Services

Subdomain: `application.domain.services.order`. Covers spec **Domain 7 — Order
lifecycle**, the central process of the system.

Lifecycle: `CART → PENDING_PAYMENT → PAID → DISPATCHED → DELIVERED`
(digital-only orders skip `DISPATCHED`). A `DELIVERED` order is finalized and
immutable (spec §11); transitions are enforced by `Order.transitionTo`.

---

## CheckoutCartService — `CheckoutCartUseCase`

Converts the buyer's active cart into an `Order`.

* **Authorization:** `ValidateBuyerCanPurchaseService`.
* **Validations:** active cart must exist and be non-empty (`DomainException`).
* **Effect:** new order with a generated identifier; one `OrderItem` per cart line
  with `unitPrice` captured from the product; total computed;
  `CART → PENDING_PAYMENT`; cart marked `CONVERTED`.
* **Audit:** `CART_CHECKOUT`, severity `INFO`, details record line count and total.
* **Ports:** `CartRepositoryPort`, `OrderRepositoryPort`.

## ProcessOrderPaymentService — `ProcessOrderPaymentUseCase`

Processes payment through `PaymentGatewayPort` (the simulation with retry lives in the
adapter).

* **Authorization:** `ValidateBuyerCanPurchaseService`.
* **Rejection:** order stays `PENDING_PAYMENT`; audit `ORDER_PAYMENT` severity `ERROR`;
  `PaymentRejectedException` is thrown so the buyer can retry.
* **Approval:** `PENDING_PAYMENT → PAID`; if the order has no physical products,
  `PAID → DELIVERED`; a confirmation `Notification` is sent via `NotificationPort`;
  audit `ORDER_PAYMENT` severity `INFO`.
* **Ports:** `OrderRepositoryPort`, `PaymentGatewayPort`, `NotificationPort`.

## DispatchOrderService — `DispatchOrderUseCase`

* **Authorization:** requester `ACTIVE` and role `LOGISTICS_OPERATOR`.
* **Validations:** order must contain physical products (`DomainException`).
* **Effect:** `PAID → DISPATCHED`.
* **Audit:** `ORDER_DISPATCH`, severity `INFO`.
* **Ports:** `OrderRepositoryPort`.

## ConfirmDeliveryService — `ConfirmDeliveryUseCase`

* **Authorization:** requester `ACTIVE` and role `LOGISTICS_OPERATOR`.
* **Effect:** `DISPATCHED → DELIVERED` (finalized).
* **Audit:** `ORDER_DELIVERY`, severity `INFO`.
* **Ports:** `OrderRepositoryPort`.

## ConsultOrderService — `ConsultOrderUseCase`

Returns an order. `ValidateOrderAccessService` — a `BUYER` only sees its own;
`ADMIN` / `SUPERVISOR` / `LOGISTICS_OPERATOR` may see any. Not audited.
