# Seller Services

## Introduction

This document defines the services belonging to the **Seller Management** subdomain of
NexusMarket (spec **Domain 3**). These services own the incorporation and consultation
of the suppliers that publish products on the marketplace.

The defining rule of this subdomain: **sellers cannot self-register; they are
incorporated by an `ADMIN`, together with their first warehouse, in a single flow**
(spec Domain 3, flow 6.1.1).

The services described here define the conceptual behavior of the subdomain. REST
contracts, persistence mappings and infrastructure concerns are documented separately
(phases 5–6).

Location: `application.domain.services.seller`.

---

## Domain Model Context

```text
Seller (extends Person, implements AuditableEntity)
 ├── identification : DocumentId   (unique across the platform — spec §11)
 ├── fullName       : String
 ├── email          : Email
 ├── role           : UserRole      (always SELLER)
 ├── status         : SellerStatus  (ACTIVE / SUSPENDED)
 ├── onboardedBy    : User          (the ADMIN who incorporated the seller)
 └── warehouses     : List<Warehouse>   (type SELLER; the first is created at onboarding)

Warehouse
 ├── identifier : String
 ├── name       : String
 ├── type       : WarehouseType   (forced to SELLER for the first warehouse)
 ├── owner      : Seller
 └── location   : String
```

---

## Service Design Principles

### Domain Model parameters

Services and Input Ports receive Domain Models, never primitive identifiers, REST
request DTOs or persistence entities.

Incorrect:

```java
Seller onboard(String adminId, String sellerDoc, String warehouseName);
```

Correct:

```java
Seller onboard(User requester, Seller seller, Warehouse firstWarehouse);
```

### External information

Services reach the outside only through `SellerRepositoryPort` and
`WarehouseRepositoryPort`. They never touch JPA, SQL or HTTP directly.

### Wiring

`@Service` + constructor injection. Unit-testable by direct instantiation with
test-double ports.

---

## 1. Onboard Seller

### Description

An `ADMIN` incorporates a new seller and creates its first warehouse in one
transaction-shaped flow (spec flow 6.1.1). The seller starts `ACTIVE`.

Input Port: `OnboardSellerUseCase` —
`Seller onboard(User requester, Seller seller, Warehouse firstWarehouse)`.

### Input

```text
User        (requester)
Seller      (carries identification, fullName, email)
Warehouse   (firstWarehouse — carries name, location)
```

### Authorization

* `ValidateUserStatusService` — requester is `ACTIVE` (spec RG01).
* `ValidateRoleAuthorizationService` — requester role is `ADMIN`.

### Domain Validations

* `firstWarehouse` must be provided (`DomainException`).
* `SellerRepositoryPort.existsByIdentification(seller)` must be `false`
  (`DuplicateUserException`, spec §11).

### Effect / State Change

* Seller: `role = SELLER`, `status = ACTIVE`, `onboardedBy = requester`; persisted.
* Warehouse: `type = SELLER`, `owner = savedSeller`; persisted.
* The warehouse is appended to `seller.warehouses` and the seller is updated.

### Persistence

```text
SellerRepositoryPort.existsByIdentification(seller)
SellerRepositoryPort.save(seller)
WarehouseRepositoryPort.save(firstWarehouse)
SellerRepositoryPort.update(savedSeller)
```

### Operation and Audit

Operation type: `SELLER_ONBOARDING`, severity `INFO`, details
`{ seller, firstWarehouse }`.

```text
requester, seller, firstWarehouse
  │
  ▼
OnboardSellerService
  ├── authorization (ADMIN, ACTIVE)
  ├── validate firstWarehouse + seller uniqueness
  ├── SellerRepositoryPort.save
  ├── WarehouseRepositoryPort.save  (type SELLER, owner = seller)
  ├── SellerRepositoryPort.update   (link warehouse)
  └── Operation (SELLER_ONBOARDING) ──► AuditLog
```

---

## 2. Consult Seller

### Description

Returns a seller by document. The result never exposes persistence entities.

Input Port: `ConsultSellerUseCase` — `Seller consult(User requester, Seller probe)`.

### Input

```text
User    (requester)
Seller  (probe — carries the identification)
```

### Authorization

* `ValidateUserStatusService` — requester is `ACTIVE`.
* `ValidateRoleAuthorizationService` — requester role is `ADMIN` or `SUPERVISOR`.

### Domain Validations

* The seller must exist (`EntityNotFoundException`).

### Operation and Audit

Not an audited operation (read-only).

---

## Output Ports

```java
interface SellerRepositoryPort {
    Seller save(Seller seller);
    Optional<Seller> findByIdentification(Seller seller);
    boolean existsByIdentification(Seller seller);
    List<Seller> findAll();
    void update(Seller seller);
}

interface WarehouseRepositoryPort {
    Warehouse save(Warehouse warehouse);
    Optional<Warehouse> findByIdentifier(Warehouse warehouse);
    List<Warehouse> findByOwner(Seller owner);
    void update(Warehouse warehouse);
}

interface OperationRepositoryPort { Operation save(Operation operation); /* ... */ }
interface AuditLogRepositoryPort  { AuditLog  save(AuditLog auditLog);   /* ... */ }
```

See [Output Ports](../Output%20Ports.md) for the full contracts.

---

## Input Ports

```java
interface OnboardSellerUseCase { Seller onboard(User requester, Seller seller, Warehouse firstWarehouse); }
interface ConsultSellerUseCase { Seller consult(User requester, Seller probe); }
```

---

## Onboarding Flow

```text
HTTP Request  (Phase 6)
     │
     ▼
Request DTO ──► Request Mapper ──► User (requester), Seller, Warehouse
     │
     ▼
OnboardSellerUseCase
     │
     ▼
OnboardSellerService
     ├── ValidateUserStatusService
     ├── ValidateRoleAuthorizationService (ADMIN)
     ├── SellerRepositoryPort.existsByIdentification
     ├── SellerRepositoryPort.save
     ├── WarehouseRepositoryPort.save
     ├── SellerRepositoryPort.update
     └── RegisterOperationAndAuditService (SELLER_ONBOARDING)
```
