# Inventory Services

Subdomain: `application.domain.services.inventory`. Covers spec **Domain 6 — Inventory
Management** and the critical validations of spec §11.

Inventory is always bound to a `(product, warehouse)` pair. Stock is never negative;
`DAMAGED` stock cannot be reserved. Every stock change appends an `InventoryMovement`
and records an `Operation` + `AuditLog`.

Authorization: requester `ACTIVE` and role `SELLER` or `LOGISTICS_OPERATOR`
(spec §12), except `ConsultInventoryService` which also allows `ADMIN` / `SUPERVISOR`.

---

## RegisterInventoryEntryService — `RegisterInventoryEntryUseCase`

Records incoming stock; creates the inventory item (condition `AVAILABLE`, stock 0) on
first entry.

* **Validations:** quantity > 0.
* **Effect:** `stock += quantity`; `ENTRY` movement appended.
* **Audit:** `INVENTORY_ENTRY`, severity `INFO`.

## ReserveInventoryService — `ReserveInventoryUseCase`

Reserves stock for an order.

* **Validations:** quantity > 0; the inventory item must exist
  (`InvalidReservationException`); its condition must not be `DAMAGED`
  (`InvalidReservationException`); the resulting stock must not be negative
  (`NegativeStockException`).
* **Effect:** `stock -= quantity`; `RESERVATION` movement appended.
* **Audit:** `INVENTORY_RESERVATION`, severity `INFO`.
* **Returns:** the `InventoryMovement`.

## ReleaseReservationService — `ReleaseReservationUseCase`

Returns previously reserved stock (e.g. an expired unpaid order).

* **Effect:** `stock += quantity`; recorded as an `ADJUSTMENT` movement.
* **Audit:** `INVENTORY_RELEASE`, severity `INFO`.

## AdjustInventoryService — `AdjustInventoryUseCase`

Manually corrects stock to a new absolute quantity.

* **Effect:** stock set to `newQuantity`; `ADJUSTMENT` movement records the absolute
  delta.
* **Audit:** `INVENTORY_ADJUSTMENT`, severity `WARNING`, details record previous/new
  stock.

## ConsultInventoryService — `ConsultInventoryUseCase`

Returns the stock of a product across warehouses. Not audited.

---

**Ports (all):** `InventoryRepositoryPort` (`save`, `findByProductAndWarehouse`,
`findByProduct`, `update`, `saveMovement`, `findMovements`).
