# Buyer Services

Subdomain: `application.domain.services.buyer`. Covers spec **Domain 2 — Buyer
Management**. A buyer never manages information belonging to another buyer, nor
inventory (spec RG03).

---

## RegisterBuyerService — `RegisterBuyerUseCase`

Self-service buyer registration (spec Domain 2). No requester.

* **Validations:** `mainAddress` required; document and e-mail unique across the
  platform (`DuplicateUserException`, spec §11).
* **Effect:** role set to `BUYER`, commercial status `ACTIVE`; persisted.
* **Audit:** `USER_REGISTRATION`, severity `INFO`, `performedBy = null`
  (bootstrap action).
* **Ports:** `BuyerRepositoryPort`.

## UpdateBuyerService — `UpdateBuyerUseCase`

Updates contact data and delivery addresses.

* **Authorization:** requester `ACTIVE`; `ValidateBuyerOwnershipService` — a `BUYER`
  may only update itself; `ADMIN` / `SUPERVISOR` may update any.
* **Not audited** (low-significance self-service change).
* **Ports:** `BuyerRepositoryPort`.

## ConsultBuyerService — `ConsultBuyerUseCase`

Returns a buyer by document.

* **Authorization:** requester `ACTIVE`; `ValidateBuyerOwnershipService`.
* **Not audited.**
* **Ports:** `BuyerRepositoryPort`.
