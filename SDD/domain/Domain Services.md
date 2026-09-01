# Domain Services

## Introduction

Domain services hold business logic that does not naturally belong to a single entity.
In NexusMarket each **use case** is one service class, grouped by subdomain, exposing a
single `execute(...)` (or verb-named) method.

This document is the **high-level catalogue**. The detailed specification of each
service — inputs, authorization, domain validations, effects, persistence,
operation/audit and flows — lives in one Markdown file per subdomain under
`services/`.

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

## Shared Design Principles

1. **Domain Models only.** Every service method and Input Port receives Domain Models
   or Value Objects — never DTOs, persistence entities or bare primitive identifiers.
2. **Ports for the outside.** A service reaches external resources only through Output
   Ports. It never touches JPA, MongoDB, SQL, HTTP, a payment SDK, a JWT library or a
   password-hashing library directly.
3. **Composition over inheritance.** A use-case service composes the small
   `authorization/` guards and the `operation/` audit collaborator rather than
   duplicating those rules.
4. **Rules from models stay in the domain.** If an authorization or business decision
   can be evaluated from the supplied Domain Models, it is evaluated in the domain, not
   delegated to a port.
5. **Wiring.** `@Service` + `@RequiredArgsConstructor` (Lombok). Despite the Spring
   annotation, every service must remain unit-testable by direct instantiation with
   test-double ports — no Spring context.

---

## Cross-cutting Concerns

### Authorization (spec RG01, RG03, §12)

Every command service runs, in order:

```text
ValidateUserStatusService        (requester is ACTIVE)            — for User-driven services
ValidateBuyerCanPurchaseService  (buyer commercialStatus ACTIVE)  — for Buyer-driven services
ValidateRoleAuthorizationService (role ∈ allowed)                 — responsibility matrix
Validate<X>OwnershipService      (BUYER/SELLER touches only its own)
```

See [authorization-services.md](services/authorization-services.md).

### Operation & Audit (spec traceability)

Every significant state change ends with:

```text
RegisterOperationAndAuditService.execute(operation, severity, details)
        ├── OperationRepositoryPort.save(operation)
        └── AuditLogRepositoryPort.save(auditLog)   // append-only, MongoDB expected
```

See [operation-audit-services.md](services/operation-audit-services.md).

### Order lifecycle integrity (spec Domain 7, §11)

State transitions and the immutability of a finalized (`DELIVERED`) order are enforced
by the `Order` aggregate itself (`Order.transitionTo`, `Order.addItem`), so no service
can move an order through an illegal path.

---

## High-level Catalogue

| Subdomain | Services |
| --------- | -------- |
| **user** | RegisterUser, ChangeUserStatus, ConsultUser |
| **buyer** | RegisterBuyer *(self-service)*, UpdateBuyer, ConsultBuyer |
| **seller** | OnboardSeller *(admin: seller + first warehouse)*, ConsultSeller |
| **warehouse** | RegisterWarehouse, ConsultWarehouse |
| **catalog** | PublishProduct, UpdateProduct, ChangeProductStatus, ConsultCatalog |
| **inventory** | RegisterInventoryEntry, ReserveInventory *(spec §11 validations)*, ReleaseReservation, AdjustInventory, ConsultInventory |
| **cart** | AddCartItem, RemoveCartItem, ClearCart, ConsultCart |
| **order** | CheckoutCart, ProcessOrderPayment *(PaymentGatewayPort, retry on rejection)*, DispatchOrder, ConfirmDelivery, ConsultOrder |
| **authorization** | ValidateRoleAuthorization, ValidateUserStatus, ValidateBuyerOwnership, ValidateProductOwnership, ValidateOrderAccess, ValidateBuyerCanPurchase |
| **operation** | RegisterOperation, RegisterAuditLog, RegisterOperationAndAudit, ConsultAuditLog |

---

## Consult / read services

`ConsultUser`, `ConsultBuyer`, `ConsultSeller`, `ConsultWarehouse`,
`ConsultCatalog`, `ConsultInventory`, `ConsultCart`, `ConsultOrder` and
`ConsultAuditLog` are read-only: they validate access where applicable and return
Domain Models, but they do **not** generate an `Operation` or an `AuditLog` record.
