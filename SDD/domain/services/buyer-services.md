# Buyer Services

## Introduction

This document defines the services belonging to the **Buyer Management** subdomain of
NexusMarket (spec **Domain 2**). These services own the commercial identity of a
`Buyer`: self-service registration, data maintenance and consultation.

A fundamental restriction governs the whole subdomain: **a buyer never manages
information belonging to another buyer, nor inventory** (spec Domain 2, RG03).

The services described here define the conceptual behavior of the subdomain. REST
contracts, persistence mappings and infrastructure concerns are documented separately
(phases 5–6).

Location: `application.domain.services.buyer`.

---

## Domain Model Context

```text
Buyer (extends Person, implements AuditableEntity)
 ├── identification      : DocumentId   (unique across the platform — spec §11)
 ├── fullName            : String
 ├── email               : Email        (unique across the platform — spec §11)
 ├── role                : UserRole      (always BUYER)
 ├── commercialStatus    : BuyerCommercialStatus  (ACTIVE / SUSPENDED)
 ├── mainAddress         : String        (required — spec Domain 2)
 └── additionalAddresses : List<String>  (optional)
```

`commercialStatus` governs the ability to purchase and is checked by the cart and
order subdomains through `ValidateBuyerCanPurchaseService`.

---

## Service Design Principles

### Domain Model parameters

Services and Input Ports receive Domain Models, never primitive identifiers, REST
request DTOs or persistence entities.

Incorrect:

```java
Buyer update(String buyerId, String mainAddress);
```

Correct:

```java
Buyer update(User requester, Buyer buyer);
```

### External information

Services use the data already in the Domain Models first, and reach the outside only
through `BuyerRepositoryPort`. They never touch JPA, SQL or HTTP directly.

### Wiring

`@Service` + constructor injection. Each service is unit-testable by direct
instantiation with test-double ports.

---

## 1. Register Buyer

### Description

Self-service registration of a `Buyer` (spec Domain 2). Unlike sellers, buyers do not
require onboarding by an administrator, so this service has no requester. This is a
bootstrap action.

Input Port: `RegisterBuyerUseCase` — `Buyer register(Buyer buyer)`.

### Input

```text
Buyer   (carries identification, fullName, email, mainAddress)
```

### Authorization

None — self-service.

### Domain Validations

* `mainAddress` must be present and non-blank (`DomainException`).
* `BuyerRepositoryPort.existsByIdentification(buyer)` must be `false`
  (`DuplicateUserException`, spec §11).
* `BuyerRepositoryPort.existsByEmail(buyer)` must be `false` (`DuplicateUserException`).

### Effect / State Change

* `role` is set to `BUYER`.
* `commercialStatus` is set to `ACTIVE`.
* The buyer is persisted.

### Persistence

```text
BuyerRepositoryPort.existsByIdentification(buyer)
BuyerRepositoryPort.existsByEmail(buyer)
BuyerRepositoryPort.save(buyer)
```

### Operation and Audit

Operation type: `USER_REGISTRATION`, severity `INFO`, `performedBy = null` (bootstrap),
details `{ buyer }`.

```text
Buyer
  │
  ▼
RegisterBuyerService
  ├── validate mainAddress
  ├── uniqueness checks
  ├── BuyerRepositoryPort.save
  └── Operation (USER_REGISTRATION) ──► AuditLog
```

---

## 2. Update Buyer

### Description

Updates a buyer's contact data and delivery addresses. A buyer may only update its own
record (spec RG03). This is **not** an audited operation (low-significance
self-service change).

Input Port: `UpdateBuyerUseCase` — `Buyer update(User requester, Buyer buyer)`.

### Input

```text
User    (requester)
Buyer   (carries the identification plus the new values)
```

### Authorization

* `ValidateUserStatusService` — requester is `ACTIVE`.
* `ValidateBuyerOwnershipService` — `ADMIN` / `SUPERVISOR` may update any buyer; a
  `BUYER` only if its `identification` matches the target's.

### Domain Validations

* The buyer must exist (`BuyerRepositoryPort.findByIdentification`), otherwise
  `EntityNotFoundException`.

### Effect / State Change

* `fullName`, `phoneNumber`, `address`, `mainAddress` and `additionalAddresses` are
  copied onto the stored buyer. `commercialStatus`, `role` and `identification` are
  not changed here.

### Persistence

```text
BuyerRepositoryPort.findByIdentification(buyer)
BuyerRepositoryPort.update(storedBuyer)
```

### Operation and Audit

Not audited.

---

## 3. Consult Buyer

### Description

Returns a buyer by document. The result never exposes persistence entities.

Input Port: `ConsultBuyerUseCase` — `Buyer consult(User requester, Buyer probe)`.

### Input

```text
User    (requester)
Buyer   (probe — carries the identification)
```

### Authorization

* `ValidateUserStatusService` — requester is `ACTIVE`.
* `ValidateBuyerOwnershipService` — a `BUYER` may read only its own data;
  `ADMIN` / `SUPERVISOR` may read any.

### Domain Validations

* The buyer must exist (`EntityNotFoundException`).

### Operation and Audit

Not an audited operation (read-only).

---

## Output Ports

```java
interface BuyerRepositoryPort {
    Buyer save(Buyer buyer);
    Optional<Buyer> findByIdentification(Buyer buyer);
    boolean existsByIdentification(Buyer buyer);
    boolean existsByEmail(Buyer buyer);
    void update(Buyer buyer);
}

interface OperationRepositoryPort { Operation save(Operation operation); /* ... */ }
interface AuditLogRepositoryPort  { AuditLog  save(AuditLog auditLog);   /* ... */ }
```

See [Output Ports](../Output%20Ports.md) for the full contracts.

---

## Input Ports

```java
interface RegisterBuyerUseCase { Buyer register(Buyer buyer); }
interface UpdateBuyerUseCase   { Buyer update(User requester, Buyer buyer); }
interface ConsultBuyerUseCase  { Buyer consult(User requester, Buyer probe); }
```
