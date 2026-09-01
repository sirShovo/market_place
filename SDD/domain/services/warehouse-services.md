# Warehouse Services

Subdomain: `application.domain.services.warehouse`. Covers spec **Domain 4 — Warehouse
Management**. Warehouses are classified as `MARKETPLACE` or `SELLER`.

---

## RegisterWarehouseService — `RegisterWarehouseUseCase`

An `ADMIN` registers an additional warehouse (the seller's first one is created by
`OnboardSellerService`).

* **Authorization:** requester `ACTIVE` and role `ADMIN`.
* **Validations:** a `SELLER` warehouse must reference an `owner`; a `MARKETPLACE`
  warehouse must not (`DomainException`).
* **Audit:** `WAREHOUSE_REGISTRATION`, severity `INFO`.
* **Ports:** `WarehouseRepositoryPort`.

## ConsultWarehouseService — `ConsultWarehouseUseCase`

Returns a warehouse by identifier.

* **Authorization:** requester `ACTIVE` and role `ADMIN`, `SUPERVISOR` or
  `LOGISTICS_OPERATOR`.
* **Not audited.**
* **Ports:** `WarehouseRepositoryPort`.
