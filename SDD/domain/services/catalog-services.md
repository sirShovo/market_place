# Catalog Services

Subdomain: `application.domain.services.catalog`. Covers spec **Domain 5 — Catalog
Management**. The catalog distinguishes physical and digital products; each carries
variants and a status (PUBLISHED / SUSPENDED / DISCONTINUED).

`ValidateProductOwnershipService` — a `SELLER` may only manage its own products;
`ADMIN` may manage any.

---

## PublishProductService — `PublishProductUseCase`

A `SELLER` publishes a product (spec §12).

* **Authorization:** requester `ACTIVE` and role `SELLER`.
* **Validations:** `seller` and `price` required (`DomainException`).
* **Effect:** status set to `PUBLISHED`; persisted (`PhysicalProduct` or
  `DigitalProduct`).
* **Audit:** `PRODUCT_PUBLICATION`, severity `INFO`.
* **Ports:** `ProductRepositoryPort`.

## UpdateProductService — `UpdateProductUseCase`

Updates descriptive data, price, category and variants of an existing product.

* **Authorization:** requester `ACTIVE`; `ValidateProductOwnershipService`.
* **Not audited** (status changes are handled separately).
* **Ports:** `ProductRepositoryPort`.

## ChangeProductStatusService — `ChangeProductStatusUseCase`

Moves a product between PUBLISHED / SUSPENDED / DISCONTINUED.

* **Authorization:** requester `ACTIVE`; `ValidateProductOwnershipService`.
* **Audit:** `PRODUCT_STATUS_CHANGE`, severity `INFO`, details record previous/new
  status.
* **Ports:** `ProductRepositoryPort`.

## ConsultCatalogService — `ConsultCatalogUseCase`

Lists published products (public catalog, spec flow 6.1 step 4). No requester, not
audited. **Ports:** `ProductRepositoryPort`.
