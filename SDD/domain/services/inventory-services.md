# Inventory Services

## Introduction

This document defines the services belonging to the **Inventory Management** subdomain
of NexusMarket (spec **Domain 6**) and the critical validations of spec **§11**.

Inventory is **distributed**: every `InventoryItem` is bound to exactly one product
**and** one warehouse. Two rules dominate the subdomain:

* **Stock is never negative** under any circumstance (spec Domain 6, §11).
* **`DAMAGED` stock cannot be reserved** (spec §11).

Every stock change appends an immutable `InventoryMovement` (the traceability record)
and generates an `Operation` + `AuditLog` record.

The services described here define the conceptual behavior of the subdomain. REST
contracts, persistence mappings and infrastructure concerns are documented separately
(phases 5–6).

Location: `application.domain.services.inventory`.

---

## Domain Model Context

```text
InventoryItem (implements AuditableEntity)
 ├── id        : Long
 ├── product   : Product
 ├── warehouse : Warehouse
 ├── stock     : StockQuantity            (int wrapper, rejects negative values)
 └── condition : InventoryItemCondition   (AVAILABLE / DAMAGED)

InventoryMovement   (append-only)
 ├── id            : Long
 ├── inventoryItem : InventoryItem
 ├── type          : InventoryMovementType  (ENTRY / RESERVATION / SALE_EXIT /
 │                                            ADJUSTMENT / RETURN)
 ├── quantity      : int                    (positive)
 ├── occurredOn    : LocalDateTime
 └── performedBy   : User
```

`StockQuantity.subtract(n)` throws `NegativeStockException` when `n` exceeds the
current value — the non-negative rule is enforced at the value-object level and cannot
be bypassed.

---

## Service Design Principles

### Domain Model parameters

Services and Input Ports receive Domain Models. The `(product, warehouse)` pair is
carried as an `InventoryItem` *probe* — a partially populated model used to locate the
real item — never as two `String` identifiers.

Incorrect:

```java
InventoryMovement reserve(String productId, String warehouseId, int qty);
```

Correct:

```java
InventoryMovement reserve(User requester, InventoryItem probe, int quantity);
```

### Authorization

All services require the requester to be `ACTIVE` (spec RG01) and to hold role
`SELLER` or `LOGISTICS_OPERATOR` (spec responsibility matrix §12).
`ConsultInventoryService` additionally allows `ADMIN` / `SUPERVISOR`. **Buyers never
see inventory** (spec Domain 2, RG03).

### External information

Services reach the outside only through `InventoryRepositoryPort`. They never touch
JPA, SQL or HTTP directly.

### Wiring

`@Service` + constructor injection. Unit-testable by direct instantiation with
test-double ports (see `ReserveInventoryServiceTest`).

---

## Movement Model

```text
                       ┌──────────────┐
      ENTRY  (+qty) ───►│              │
   ADJUSTMENT (=qty) ──►│  stock       │──► RESERVATION (−qty)
      RETURN (+qty) ───►│  (never < 0) │──► SALE_EXIT   (logged at dispatch)
                        └──────────────┘
```

`RELEASE` of a reservation is recorded as an `ADJUSTMENT` movement (spec Domain 6
defines only the five movement types above) with an `INVENTORY_RELEASE` operation.

---

## 1. Register Inventory Entry

### Description

Records incoming stock for a `(product, warehouse)` pair. The `InventoryItem` is
created on the first entry (condition `AVAILABLE`, stock `0`) and then incremented.

Input Port: `RegisterInventoryEntryUseCase` —
`InventoryItem registerEntry(User requester, InventoryItem probe, int quantity)`.

### Input

```text
User            (requester)
InventoryItem   (probe — carries product + warehouse)
int             (quantity)
```

### Authorization

`ACTIVE` requester with role `SELLER` or `LOGISTICS_OPERATOR`.

### Domain Validations

* `quantity > 0` (`DomainException`).

### Effect / State Change

* If no item exists for `(product, warehouse)`, one is created and saved
  (`AVAILABLE`, stock `0`).
* `stock += quantity`.
* An `ENTRY` movement is appended.

### Persistence

```text
InventoryRepositoryPort.findByProductAndWarehouse(probe)
InventoryRepositoryPort.save(freshItem)          (first entry only)
InventoryRepositoryPort.update(item)
InventoryRepositoryPort.saveMovement(ENTRY)
```

### Operation and Audit

Operation type: `INVENTORY_ENTRY`, severity `INFO`, details
`{ quantity, resultingStock }`.

---

## 2. Reserve Inventory

### Description

Reserves stock for an order. This is the service that enforces the spec §11 critical
validations.

Input Port: `ReserveInventoryUseCase` —
`InventoryMovement reserve(User requester, InventoryItem probe, int quantity)`.

### Input

```text
User            (requester)
InventoryItem   (probe — carries product + warehouse)
int             (quantity)
```

### Authorization

`ACTIVE` requester with role `SELLER` or `LOGISTICS_OPERATOR`.

### Domain Validations

| Check | Failure |
| ----- | ------- |
| `quantity > 0` | `InvalidReservationException` |
| An `InventoryItem` exists for `(product, warehouse)` | `InvalidReservationException` ("inventory does not exist") |
| `item.condition != DAMAGED` | `InvalidReservationException` ("damaged inventory cannot be reserved") |
| `item.stock - quantity >= 0` | `NegativeStockException` (raised by `StockQuantity.subtract`) |

### Effect / State Change

* `stock -= quantity`.
* A `RESERVATION` movement is appended and returned.

### Persistence

```text
InventoryRepositoryPort.findByProductAndWarehouse(probe)
InventoryRepositoryPort.update(item)
InventoryRepositoryPort.saveMovement(RESERVATION)
```

### Operation and Audit

Operation type: `INVENTORY_RESERVATION`, severity `INFO`, details
`{ quantity, remainingStock }`.

```text
probe, quantity
  │
  ▼
ReserveInventoryService
  ├── authorization
  ├── quantity > 0 ?                        ── no ─► InvalidReservationException
  ├── item found ?                          ── no ─► InvalidReservationException
  ├── condition != DAMAGED ?                ── no ─► InvalidReservationException
  ├── stock.subtract(quantity)              ── < 0 ─► NegativeStockException
  ├── InventoryRepositoryPort.update
  ├── saveMovement(RESERVATION)
  └── Operation (INVENTORY_RESERVATION) ──► AuditLog
```

---

## 3. Release Reservation

### Description

Returns previously reserved stock to available inventory — for example when an unpaid
order expires (the Phase 7 scheduler is a consumer).

Input Port: `ReleaseReservationUseCase` —
`InventoryMovement release(User requester, InventoryItem probe, int quantity)`.

### Input

```text
User            (requester)
InventoryItem   (probe — carries product + warehouse)
int             (quantity)
```

### Authorization

`ACTIVE` requester with role `SELLER` or `LOGISTICS_OPERATOR`.

### Domain Validations

* `quantity > 0` (`DomainException`).
* The inventory item must exist (`EntityNotFoundException`).

### Effect / State Change

* `stock += quantity`.
* Recorded as an `ADJUSTMENT` movement.

### Persistence

```text
InventoryRepositoryPort.findByProductAndWarehouse(probe)
InventoryRepositoryPort.update(item)
InventoryRepositoryPort.saveMovement(ADJUSTMENT)
```

### Operation and Audit

Operation type: `INVENTORY_RELEASE`, severity `INFO`, details
`{ released, resultingStock }`.

---

## 4. Adjust Inventory

### Description

Manually corrects the stock of an inventory item to a new **absolute** quantity.

Input Port: `AdjustInventoryUseCase` —
`InventoryMovement adjust(User requester, InventoryItem probe, int newQuantity)`.

### Input

```text
User            (requester)
InventoryItem   (probe — carries product + warehouse)
int             (newQuantity)
```

### Authorization

`ACTIVE` requester with role `SELLER` or `LOGISTICS_OPERATOR`.

### Domain Validations

* The inventory item must exist (`EntityNotFoundException`).
* `newQuantity >= 0` (enforced by `StockQuantity.of` → `NegativeStockException`).

### Effect / State Change

* `stock` is set to `newQuantity`.
* An `ADJUSTMENT` movement records the absolute delta
  `|newQuantity − previousStock|`.

### Persistence

```text
InventoryRepositoryPort.findByProductAndWarehouse(probe)
InventoryRepositoryPort.update(item)
InventoryRepositoryPort.saveMovement(ADJUSTMENT)
```

### Operation and Audit

Operation type: `INVENTORY_ADJUSTMENT`, severity `WARNING`, details
`{ previousStock, newStock }`.

---

## 5. Consult Inventory

### Description

Returns the stock of a product across all warehouses.

Input Port: `ConsultInventoryUseCase` —
`List<InventoryItem> consult(User requester, Product product)`.

### Input

```text
User      (requester)
Product   (the product to inspect)
```

### Authorization

`ACTIVE` requester with role `SELLER`, `LOGISTICS_OPERATOR`, `ADMIN` or `SUPERVISOR`.

### Persistence

```text
InventoryRepositoryPort.findByProduct(product)
```

### Operation and Audit

Not an audited operation (read-only).

---

## Output Ports

```java
interface InventoryRepositoryPort {
    InventoryItem save(InventoryItem item);
    Optional<InventoryItem> findByProductAndWarehouse(InventoryItem probe);
    List<InventoryItem> findByProduct(Product product);
    void update(InventoryItem item);
    InventoryMovement saveMovement(InventoryMovement movement);
    List<InventoryMovement> findMovements(InventoryItem item);
}

interface OperationRepositoryPort { Operation save(Operation operation); /* ... */ }
interface AuditLogRepositoryPort  { AuditLog  save(AuditLog auditLog);   /* ... */ }
```

See [Output Ports](../Output%20Ports.md) for the full contracts.

---

## Input Ports

```java
interface RegisterInventoryEntryUseCase { InventoryItem registerEntry(User requester, InventoryItem probe, int quantity); }
interface ReserveInventoryUseCase       { InventoryMovement reserve(User requester, InventoryItem probe, int quantity); }
interface ReleaseReservationUseCase     { InventoryMovement release(User requester, InventoryItem probe, int quantity); }
interface AdjustInventoryUseCase        { InventoryMovement adjust(User requester, InventoryItem probe, int newQuantity); }
interface ConsultInventoryUseCase       { List<InventoryItem> consult(User requester, Product product); }
```

---

## Reservation Flow

```text
Order checkout  (order subdomain)
        │  requests stock for each physical line
        ▼
ReserveInventoryUseCase
        │
        ▼
ReserveInventoryService
        ├── authorization (SELLER / LOGISTICS_OPERATOR, ACTIVE)
        ├── locate InventoryItem via InventoryRepositoryPort
        ├── spec §11 validations (exists, not DAMAGED, non-negative)
        ├── stock -= quantity
        ├── InventoryRepositoryPort.update
        ├── InventoryRepositoryPort.saveMovement(RESERVATION)
        └── RegisterOperationAndAuditService (INVENTORY_RESERVATION)
```
