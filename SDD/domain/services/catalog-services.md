# Catalog Services

## Introduction

This document defines the services belonging to the **Catalog Management** subdomain
of NexusMarket (spec **Domain 5**). These services own the product catalog: publishing
products, maintaining their data and variants, and controlling their catalog status.

The catalog differentiates **physical products** (require inventory and dispatch) from
**digital products** (delivered immediately after payment). Each product carries a
list of **variants** (colour / size / model) and a **status**
(PUBLISHED / SUSPENDED / DISCONTINUED).

The services described here define the conceptual behavior of the subdomain. REST
contracts, persistence mappings and infrastructure concerns are documented separately
(phases 5–6).

Location: `application.domain.services.catalog`.

---

## Domain Model Context

```text
Product (abstract, implements AuditableEntity)
 ├── identifier   : String
 ├── name         : String        (not empty)
 ├── description  : String
 ├── type         : ProductType   (PHYSICAL / DIGITAL — from the concrete subclass)
 ├── status       : ProductStatus (PUBLISHED / SUSPENDED / DISCONTINUED)
 ├── price        : Money          (non-negative)
 ├── seller       : Seller
 ├── category     : Category
 └── variants     : List<ProductVariant>

PhysicalProduct extends Product   →  getType() == PHYSICAL
DigitalProduct  extends Product   →  getType() == DIGITAL

ProductVariant
 ├── attributeName : String   (e.g. "color", "size", "model")
 └── value         : String
```

---

## Service Design Principles

### Domain Model parameters

Services and Input Ports receive Domain Models, never primitive identifiers, REST
request DTOs or persistence entities.

Incorrect:

```java
Product publish(String sellerId, String name, BigDecimal price, String type);
```

Correct:

```java
Product publish(User requester, Product product);
```

### Ownership

`ValidateProductOwnershipService` is the shared guard for the mutating services:
`ADMIN` may manage any product; a `SELLER` only its own (the product's
`seller.identification` must match the requester's).

### External information

Services reach the outside only through `ProductRepositoryPort` (and
`CategoryRepositoryPort` when categories must be resolved). They never touch JPA, SQL
or HTTP directly.

### Wiring

`@Service` + constructor injection. Unit-testable by direct instantiation with
test-double ports.

---

## 1. Publish Product

### Description

A `SELLER` publishes a product to the catalog (spec responsibility matrix §12). The
concrete type (`PhysicalProduct` / `DigitalProduct`) is decided by the caller; the
product is created `PUBLISHED`.

Input Port: `PublishProductUseCase` — `Product publish(User requester, Product product)`.

### Input

```text
User      (requester)
Product   (PhysicalProduct or DigitalProduct — carries name, description, price,
           seller, category, variants)
```

### Authorization

* `ValidateUserStatusService` — requester is `ACTIVE` (spec RG01).
* `ValidateRoleAuthorizationService` — requester role is `SELLER`.

### Domain Validations

* `product.seller` must be set (`DomainException`).
* `product.price` must be set (`DomainException`).

### Effect / State Change

* `status` is set to `PUBLISHED`.
* The product is persisted.

### Persistence

```text
ProductRepositoryPort.save(product)
```

### Operation and Audit

Operation type: `PRODUCT_PUBLICATION`, severity `INFO`, details `{ product, type }`.

---

## 2. Update Product

### Description

A seller updates its own product's descriptive data, price, category and variants.
Status changes are handled by `ChangeProductStatusService`, not here. Not an audited
operation.

Input Port: `UpdateProductUseCase` — `Product update(User requester, Product product)`.

### Input

```text
User      (requester)
Product   (carries the identifier plus the new values)
```

### Authorization

* `ValidateUserStatusService` — requester is `ACTIVE`.
* `ValidateProductOwnershipService` — evaluated against the **stored** product.

### Domain Validations

* The product must exist (`ProductRepositoryPort.findByIdentifier`), otherwise
  `EntityNotFoundException`.

### Effect / State Change

* `name`, `description`, `price`, `category` and `variants` are copied onto the stored
  product. `status`, `seller`, `type` and `identifier` are not changed here.

### Persistence

```text
ProductRepositoryPort.findByIdentifier(product)
ProductRepositoryPort.update(storedProduct)
```

### Operation and Audit

Not audited.

---

## 3. Change Product Status

### Description

Moves a product between `PUBLISHED`, `SUSPENDED` and `DISCONTINUED` (spec Domain 5).
The desired status travels on the supplied `product` model.

Input Port: `ChangeProductStatusUseCase` —
`Product changeStatus(User requester, Product product)`.

### Input

```text
User      (requester)
Product   (carries the identifier and the desired status)
```

### Authorization

* `ValidateUserStatusService` — requester is `ACTIVE`.
* `ValidateProductOwnershipService` — evaluated against the stored product.

### Domain Validations

* The product must exist (`EntityNotFoundException`).

### Effect / State Change

* The stored product's `status` is set to the requested value.

### Persistence

```text
ProductRepositoryPort.findByIdentifier(product)
ProductRepositoryPort.update(storedProduct)
```

### Operation and Audit

Operation type: `PRODUCT_STATUS_CHANGE`, severity `INFO`, details
`{ product, previousStatus, newStatus }`.

---

## 4. Consult Catalog

### Description

Returns the list of published products. This is the public catalog seen by buyers
(spec flow 6.1 step 4), so it takes no requester and performs no authorization.

Input Port: `ConsultCatalogUseCase` — `List<Product> consultPublished()`.

### Input

None.

### Persistence

```text
ProductRepositoryPort.findPublished()
```

### Operation and Audit

Not an audited operation (read-only).

---

## Output Ports

```java
interface ProductRepositoryPort {
    Product save(Product product);
    Optional<Product> findByIdentifier(Product product);
    List<Product> findBySeller(Seller seller);
    List<Product> findPublished();
    void update(Product product);
}

interface CategoryRepositoryPort {
    Category save(Category category);
    Optional<Category> findById(Category category);
    List<Category> findAll();
}

interface OperationRepositoryPort { Operation save(Operation operation); /* ... */ }
interface AuditLogRepositoryPort  { AuditLog  save(AuditLog auditLog);   /* ... */ }
```

See [Output Ports](../Output%20Ports.md) for the full contracts.

---

## Input Ports

```java
interface PublishProductUseCase      { Product publish(User requester, Product product); }
interface UpdateProductUseCase       { Product update(User requester, Product product); }
interface ChangeProductStatusUseCase { Product changeStatus(User requester, Product product); }
interface ConsultCatalogUseCase      { List<Product> consultPublished(); }
```

---

## Publication Flow

```text
HTTP Request  (Phase 6)
     │
     ▼
Request DTO ──► Request Mapper ──► User (requester), Product (Physical/Digital)
     │
     ▼
PublishProductUseCase
     │
     ▼
PublishProductService
     ├── ValidateUserStatusService
     ├── ValidateRoleAuthorizationService (SELLER)
     ├── validate seller + price
     ├── ProductRepositoryPort.save  (status PUBLISHED)
     └── RegisterOperationAndAuditService (PRODUCT_PUBLICATION)
```
