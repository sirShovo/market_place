# Seller Services

Subdomain: `application.domain.services.seller`. Covers spec **Domain 3 — Seller
Management**. Sellers cannot self-register (spec Domain 3).

---

## OnboardSellerService — `OnboardSellerUseCase`

An `ADMIN` registers a seller **and its first warehouse** in a single flow
(spec flow 6.1.1).

* **Authorization:** requester `ACTIVE` and role `ADMIN`.
* **Validations:** first warehouse required (`DomainException`); seller document unique
  (`DuplicateUserException`).
* **Effect:** seller role `SELLER`, status `ACTIVE`, `onboardedBy = requester`;
  warehouse forced to type `SELLER` with `owner = seller`; both persisted and linked.
* **Audit:** `SELLER_ONBOARDING`, severity `INFO`, details record seller id and first
  warehouse id.
* **Ports:** `SellerRepositoryPort`, `WarehouseRepositoryPort`.

## ConsultSellerService — `ConsultSellerUseCase`

Returns a seller by document.

* **Authorization:** requester `ACTIVE` and role `ADMIN` or `SUPERVISOR`.
* **Not audited.**
* **Ports:** `SellerRepositoryPort`.
