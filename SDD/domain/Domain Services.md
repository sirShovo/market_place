# Domain Services

## Introduction

Domain services hold business logic that does not belong to a single entity. In
NexusMarket each **use case** is one service class, grouped by subdomain, exposing a
single `execute(...)` method. Services:

* validate business rules using information already in the Domain Models;
* reach outside only through Output Ports;
* compose the `authorization/` services for access control (spec RG01, RG03, §12);
* compose `RegisterOperationAndAuditService` so every significant action produces an
  `Operation` and an `AuditLog` record (spec traceability requirements).

Location: `application.domain.services.<subdomain>`. Wiring: `@Service` +
`@RequiredArgsConstructor`. Each service is unit-testable by direct instantiation with
test-double ports.

Detailed specs per subdomain:

```
services/
├── user-services.md
├── buyer-services.md
├── seller-services.md
├── warehouse-services.md
├── catalog-services.md
├── inventory-services.md
├── cart-services.md
├── order-services.md
├── authorization-services.md
└── operation-audit-services.md
```

---

# High-level catalogue

## User (`services/user`)
- **RegisterUserService** — registers a system user; a `SELLER` user may only be
  created by an `ADMIN` (spec Domain 3); document and e-mail are unique (spec §11).
- **ChangeUserStatusService** — activates / blocks / deactivates a user (`ADMIN`).
- **ConsultUserService** — returns a user, subject to the requester's permissions.

## Buyer (`services/buyer`)
- **RegisterBuyerService** — self-service buyer registration (spec Domain 2).
- **UpdateBuyerService** — updates addresses / data; a buyer may only update itself
  (spec RG03).
- **ConsultBuyerService** — returns a buyer; a buyer can only read its own data.

## Seller (`services/seller`)
- **OnboardSellerService** — `ADMIN` registers a seller **and its first warehouse** in
  one flow (spec flow 6.1.1).
- **ConsultSellerService** — returns a seller (`ADMIN` / `SUPERVISOR`).

## Warehouse (`services/warehouse`)
- **RegisterWarehouseService** — `ADMIN` registers an additional warehouse
  (MARKETPLACE or SELLER).
- **ConsultWarehouseService** — returns a warehouse.

## Catalog (`services/catalog`)
- **PublishProductService** — a `SELLER` publishes a product (physical or digital).
- **UpdateProductService** — a seller updates its own product.
- **ChangeProductStatusService** — PUBLISHED / SUSPENDED / DISCONTINUED.
- **ConsultCatalogService** — lists published products.

## Inventory (`services/inventory`)
- **RegisterInventoryEntryService** — records an `ENTRY` movement, increasing stock.
- **ReserveInventoryService** — reserves stock for an order; rejects non-existent or
  `DAMAGED` inventory and any move that would go negative (spec Domain 6, §11).
- **ReleaseReservationService** — returns previously reserved stock.
- **AdjustInventoryService** — manual `ADJUSTMENT` to a new quantity.
- **ConsultInventoryService** — stock of a product across warehouses.

## Cart (`services/cart`)
- **AddCartItemService**, **RemoveCartItemService**, **ClearCartService**,
  **ConsultCartService** — manage the buyer's single active cart.

## Order (`services/order`)
- **CheckoutCartService** — converts the active cart into an `Order`
  (`CART → PENDING_PAYMENT`), capturing unit prices.
- **ProcessOrderPaymentService** — calls `PaymentGatewayPort`; on approval the order
  becomes `PAID`; a digital-only order goes straight to `DELIVERED`; on rejection it
  stays `PENDING_PAYMENT` and the buyer may retry.
- **DispatchOrderService** — `LOGISTICS_OPERATOR` moves `PAID → DISPATCHED` and emits
  `SALE_EXIT` inventory movements.
- **ConfirmDeliveryService** — `LOGISTICS_OPERATOR` moves `DISPATCHED → DELIVERED`
  (finalized, immutable).
- **ConsultOrderService** — returns an order; a buyer only sees its own.

## Authorization (`services/authorization`)
- **ValidateRoleAuthorizationService** — requester holds one of the allowed roles.
- **ValidateUserStatusService** — requester is `ACTIVE` (spec RG01).
- **ValidateBuyerOwnershipService** — a buyer only touches its own data (spec RG03).

## Operation & Audit (`services/operation`)
- **RegisterOperationService** — persists an `Operation`.
- **RegisterAuditLogService** — persists an `AuditLog` record.
- **RegisterOperationAndAuditService** — composes the two; called by every command
  service.
- **ConsultAuditLogService** — reads audit history (`ADMIN` / `SUPERVISOR`).
