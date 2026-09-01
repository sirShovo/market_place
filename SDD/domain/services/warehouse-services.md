# Warehouse Services

## Introduction

This document defines the services belonging to the **Warehouse Management** subdomain
of NexusMarket (spec **Domain 4**). These services control the physical storage
locations where inventory is held.

Warehouses are classified as **`MARKETPLACE`** (operated directly by the marketplace)
or **`SELLER`** (owned by a specific seller). A seller's *first* warehouse is created
by `OnboardSellerService`; this subdomain registers any *additional* warehouse.

The services described here define the conceptual behavior of the subdomain. REST
contracts, persistence mappings and infrastructure concerns are documented separately
(phases 5–6).

Location: `application.domain.services.warehouse`.

---

## Domain Model Context

```text
Warehouse (implements AuditableEntity)
 ├── identifier : String
 ├── name       : String        (not empty)
 ├── type       : WarehouseType (MARKETPLACE / SELLER)
 ├── owner      : Seller?        (present iff type == SELLER)
 └── location   : String
```

---

## Service Design Principles

### Domain Model parameters

Services and Input Ports receive Domain Models, never primitive identifiers, REST
request DTOs or persistence entities.

Incorrect:

```java
Warehouse register(String adminId, String name, String type);
```

Correct:

```java
Warehouse register(User requester, Warehouse warehouse);
```

### External information

Services reach the outside only through `WarehouseRepositoryPort`. They never touch
JPA, SQL or HTTP directly.

### Wiring

`@Service` + constructor injection. Unit-testable by direct instantiation with
test-double ports.

---

## 1. Register Warehouse

### Description

An `ADMIN` registers an additional warehouse (spec Domain 4). The warehouse type
determines whether an owning seller is required.

Input Port: `RegisterWarehouseUseCase` —
`Warehouse register(User requester, Warehouse warehouse)`.

### Input

```text
User        (requester)
Warehouse   (carries identifier, name, type, location, and owner when type == SELLER)
```

### Authorization

* `ValidateUserStatusService` — requester is `ACTIVE` (spec RG01).
* `ValidateRoleAuthorizationService` — requester role is `ADMIN`.

### Domain Validations

* A `SELLER` warehouse must reference an `owner` (`DomainException`).
* A `MARKETPLACE` warehouse must **not** reference an `owner` (`DomainException`).

### Effect / State Change

* The warehouse is persisted as supplied (after the type/owner consistency check).

### Persistence

```text
WarehouseRepositoryPort.save(warehouse)
```

### Operation and Audit

Operation type: `WAREHOUSE_REGISTRATION`, severity `INFO`, details
`{ warehouse, type }`.

```text
requester, warehouse
  │
  ▼
RegisterWarehouseService
  ├── authorization (ADMIN, ACTIVE)
  ├── validate type / owner consistency
  ├── WarehouseRepositoryPort.save
  └── Operation (WAREHOUSE_REGISTRATION) ──► AuditLog
```

---

## 2. Consult Warehouse

### Description

Returns a warehouse by identifier. The result never exposes persistence entities.

Input Port: `ConsultWarehouseUseCase` —
`Warehouse consult(User requester, Warehouse probe)`.

### Input

```text
User        (requester)
Warehouse   (probe — carries the identifier)
```

### Authorization

* `ValidateUserStatusService` — requester is `ACTIVE`.
* `ValidateRoleAuthorizationService` — requester role is `ADMIN`, `SUPERVISOR` or
  `LOGISTICS_OPERATOR`.

### Domain Validations

* The warehouse must exist (`EntityNotFoundException`).

### Operation and Audit

Not an audited operation (read-only).

---

## Output Ports

```java
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
interface RegisterWarehouseUseCase { Warehouse register(User requester, Warehouse warehouse); }
interface ConsultWarehouseUseCase  { Warehouse consult(User requester, Warehouse probe); }
```
