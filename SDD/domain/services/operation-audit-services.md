# Operation & Audit Services

## Introduction

This document defines the services belonging to the **Operation & Audit** subdomain of
NexusMarket. It implements the cross-cutting traceability requirement of the
specification: **every significant business action produces an `Operation` and an
immutable, append-only `AuditLog` record**.

An `Operation` represents *the action that occurred*; it is distinct from the current
status of the affected entity. The `AuditLog` preserves, among other things, the role
the performing user held *at the time the action was executed*.

`RegisterOperationAndAuditService` is composed by every command service across the
other subdomains. `ConsultAuditLogService` is the only member exposed as an Input
Port.

Location: `application.domain.services.operation`.

---

## Domain Model Context

```text
Operation
 ├── operationId    : Integer
 ├── operationType  : OperationType     (business catalog)
 ├── executionDate  : LocalDateTime
 ├── performedBy    : User              (null for bootstrap actions, e.g. buyer self-registration)
 └── affectedEntity : AuditableEntity   (User / Buyer / Seller / Warehouse / Product /
                                          InventoryItem / Cart / Order)

AuditLog   (append-only, immutable — MongoDB implementation expected)
 ├── auditId        : String
 ├── operationType  : OperationType
 ├── operationDate  : LocalDateTime
 ├── performedBy    : User
 ├── userRole       : UserRole          (role held when the action was performed)
 ├── affectedEntity : AuditableEntity
 ├── severity       : AuditSeverity     (INFO / WARNING / ERROR / CRITICAL)
 └── details        : Map<String,Object>  (operation-specific payload)
```

---

## Design Principles

* **Domain Models only** — the audit collaborator receives an `Operation`, an
  `AuditSeverity` and a `details` map; it never sees DTOs or persistence entities.
* **Two output ports** — `OperationRepositoryPort` (business operations, relational)
  and `AuditLogRepositoryPort` (audit trail, expected MongoDB). The domain is unaware
  of either technology.
* **Append-only** — neither service ever updates or deletes; `AuditLog` records are
  immutable once persisted.
* **Wiring** — `@Service` + constructor injection.

---

## 1. RegisterOperationService

```java
Operation execute(Operation operation)
```

Stamps `executionDate` when absent and persists through
`OperationRepositoryPort.save`. Returns the persisted operation.

---

## 2. RegisterAuditLogService

```java
AuditLog execute(AuditLog auditLog)
```

Stamps `operationDate` when absent and persists through
`AuditLogRepositoryPort.save`. Returns the persisted record.

---

## 3. RegisterOperationAndAuditService

```java
void execute(Operation operation, AuditSeverity severity, Map<String,Object> details)
```

The collaborator every command service calls. It:

1. persists the `Operation` via `RegisterOperationService`;
2. builds an `AuditLog` from the persisted operation, copying `operationType`,
   `performedBy`, the performer's `role` (→ `userRole`) and `affectedEntity`, and
   attaching the supplied `severity` and `details`;
3. persists the `AuditLog` via `RegisterAuditLogService`.

```text
Operation, severity, details
        │
        ▼
RegisterOperationAndAuditService
        ├── RegisterOperationService.execute(operation)
        │        └── OperationRepositoryPort.save
        └── build AuditLog(operationType, performedBy, userRole,
                           affectedEntity, severity, details)
                 └── RegisterAuditLogService.execute(auditLog)
                          └── AuditLogRepositoryPort.save
```

### Severity conventions

| Situation | Severity |
| --------- | -------- |
| Normal successful action (registration, publication, entry, reservation, payment, dispatch, delivery) | `INFO` |
| Sensitive state change (user status change, inventory adjustment) | `WARNING` |
| Failed sensitive action (payment rejected) | `ERROR` |
| Reserved for future use (integrity violations) | `CRITICAL` |

---

## 4. ConsultAuditLogService — `ConsultAuditLogUseCase`

```java
List<AuditLog> consult(User requester, AuditableEntity entity)
```

Returns the audit history for an entity.

### Authorization

* `ValidateUserStatusService` — requester is `ACTIVE`.
* `ValidateRoleAuthorizationService` — requester role is `ADMIN` or `SUPERVISOR`.

### Persistence

```text
AuditLogRepositoryPort.findByEntity(entity)
```

### Operation and Audit

Not an audited operation (read-only).

---

## Output Ports

```java
interface OperationRepositoryPort {
    Operation save(Operation operation);
    List<Operation> findByUser(User user);
    List<Operation> findByEntity(AuditableEntity entity);
}

interface AuditLogRepositoryPort {
    AuditLog save(AuditLog auditLog);
    List<AuditLog> findByUser(User user);
    List<AuditLog> findByEntity(AuditableEntity entity);
}
```

See [Output Ports](../Output%20Ports.md) for the full contracts.

---

## Input Ports

```java
interface ConsultAuditLogUseCase { List<AuditLog> consult(User requester, AuditableEntity entity); }
```

`RegisterOperationService`, `RegisterAuditLogService` and
`RegisterOperationAndAuditService` are internal collaborators with no Input Port.

---

## Consumers

Every command service in the other subdomains depends on
`RegisterOperationAndAuditService`:

```text
user      : USER_REGISTRATION, USER_STATUS_CHANGE
buyer     : USER_REGISTRATION
seller    : SELLER_ONBOARDING
warehouse : WAREHOUSE_REGISTRATION
catalog   : PRODUCT_PUBLICATION, PRODUCT_STATUS_CHANGE
inventory : INVENTORY_ENTRY, INVENTORY_RESERVATION, INVENTORY_RELEASE, INVENTORY_ADJUSTMENT
order     : CART_CHECKOUT, ORDER_PAYMENT, ORDER_DISPATCH, ORDER_DELIVERY
```
