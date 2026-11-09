# Schedulers and Bootstrap Configuration

## Overview

Phase 7 introduces the automated operational background tasks (`application.adapters.in.scheduler`), the idempotent initial administrator bootstrap (`application.infrastructure.config.DataSeeder`), and end-to-end lifecycle verification across all marketplace roles.

```text
Spring @Scheduled Trigger
      │
      ▼
OrderExpirationScheduler         (application.adapters.in.scheduler)
      │
      ▼
ReleaseExpiredOrdersUseCase      (application.domain.ports.in)
      │
      ▼
ReleaseExpiredOrdersService      (application.domain.services.order)
      ├──► BusinessConfigurationPort (unpaid expiration minutes)
      ├──► OrderRepositoryPort       (PENDING_PAYMENT orders)
      ├──► InventoryRepositoryPort   (restores stock & logs ADJUSTMENT movement)
      └──► RegisterOperationAndAuditService (INVENTORY_RELEASE operation & audit)
```

---

## 1. Unpaid Order Reservation Expiration

### 1.1 `ReleaseExpiredOrdersUseCase` & `ReleaseExpiredOrdersService`

- **Input Port:** `ReleaseExpiredOrdersUseCase` (`application.domain.ports.in.ReleaseExpiredOrdersUseCase`) exposes `int releaseExpiredOrders()`.
- **Domain Service:** `ReleaseExpiredOrdersService` (`application.domain.services.order.ReleaseExpiredOrdersService`):
  1. Queries `BusinessConfigurationPort.getUnpaidOrderExpirationMinutes()` (default `30` minutes).
  2. Computes the expiration cutoff `LocalDateTime.now().minusMinutes(expirationMinutes)`.
  3. Retrieves all orders in `OrderStatus.PENDING_PAYMENT` via `OrderRepositoryPort.findByStatus(OrderStatus.PENDING_PAYMENT)`.
  4. Filters orders whose `createdAt` timestamp is strictly before the cutoff and that still hold at least one active physical line reservation (`OrderItem.getWarehouse() != null`).
  5. For each physical `OrderItem` with an active reservation:
     - Restores `orderItem.getQuantity()` to the matching `InventoryItem` in `orderItem.getWarehouse()`.
     - Persists an `InventoryMovement` of type `InventoryMovementType.ADJUSTMENT`.
     - Records an `Operation` (`OperationType.INVENTORY_RELEASE`) and an `AuditLog` (`AuditSeverity.INFO`, details including `orderIdentifier`, `productIdentifier`, `warehouseIdentifier`, `releasedQuantity`, `reason = UNPAID_ORDER_EXPIRED`).
     - Clears `orderItem.setWarehouse(null)` so subsequent scheduler runs do not double-release the same line.
  6. Updates the order via `OrderRepositoryPort.update(order)` and returns the count of orders whose reservations were released.

### 1.2 `OrderExpirationScheduler` (`application.adapters.in.scheduler`)

- Inbound driving adapter annotated with `@Component` and `@Scheduled(fixedDelayString = "${app.scheduler.order-expiration-delay-ms:60000}")`.
- Invokes `releaseExpiredOrdersUseCase.releaseExpiredOrders()` on each tick and logs the number of expired orders processed.

---

## 2. Idempotent Administrator Bootstrap (`DataSeeder`)

Because `Seller` accounts and additional `ADMIN`, `LOGISTICS_OPERATOR`, and `SUPERVISOR` accounts can only be registered by an authenticated `ADMIN` user (spec RG01, RG03, flow 6.1.1), the platform requires an initial administrator account on first boot.

- **Component:** `DataSeeder` (`application.infrastructure.config.DataSeeder`) implements Spring Boot's `CommandLineRunner`.
- **Configuration Properties:**
  - `app.seed.enabled` (default `true`)
  - `app.seed.admin.username` (default `admin`)
  - `app.seed.admin.password` (default `admin123`)
  - `app.seed.admin.identification` (default `ADMIN-001`)
  - `app.seed.admin.email` (default `admin@nexusmarket.com`)
  - `app.seed.admin.full-name` (default `System Administrator`)
- **Behavior:**
  1. When `app.seed.enabled=false`, exits immediately.
  2. Checks whether the configured admin username, document ID, or email already exists in `UserRepositoryPort`.
  3. If absent, hashes the password via `PasswordServicePort.encrypt`, saves the `User` with `UserRole.ADMIN` and `UserStatus.ACTIVE`, and records a `USER_REGISTRATION` operation and `AuditLog`.
