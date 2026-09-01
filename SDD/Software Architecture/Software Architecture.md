# Software Architecture

## Overview

The **NexusMarket API** is built using **Hexagonal Architecture (Ports and Adapters)**
combined with **Domain-Driven Design (DDD)** principles.

The primary objective of this architecture is to isolate the business domain from
external technologies, ensuring that business rules remain independent from frameworks,
databases, communication protocols, and infrastructure concerns.

This approach promotes maintainability, scalability, testability, and technology
independence.

---

# Architectural Principles

- Domain-first design.
- Separation of concerns.
- Dependency inversion.
- Technology independence.
- High cohesion.
- Low coupling.
- Explicit boundaries between layers.

The domain contains all business rules and never depends on external technologies.

---

# Architecture Layers

```
Application
│
├── Adapters
│
├── Domain
│
└── Infrastructure
```

---

# Package Structure

```text
src/
└── main/
    └── java/
        └── application/
            │
            ├── NexusMarketApplication.java
            │
            ├── adapters/
            │   ├── in/
            │   │   └── rest/
            │   │       ├── controllers/
            │   │       ├── requests/
            │   │       ├── responses/
            │   │       └── mappers/
            │   │
            │   └── out/
            │       ├── persistence/
            │       │   ├── mysql/
            │       │   │   ├── adapters/
            │       │   │   ├── entities/
            │       │   │   ├── repositories/
            │       │   │   └── mappers/
            │       │   └── mongodb/
            │       │       ├── adapters/
            │       │       ├── documents/
            │       │       ├── repositories/
            │       │       └── mappers/
            │       │
            │       ├── payment/          (PaymentGatewayPort implementation + simulation)
            │       └── notification/     (NotificationPort implementation)
            │
            ├── domain/
            │   ├── models/
            │   ├── valueobjects/
            │   ├── enums/
            │   ├── services/
            │   │   ├── user/
            │   │   ├── buyer/
            │   │   ├── seller/
            │   │   ├── warehouse/
            │   │   ├── catalog/
            │   │   ├── inventory/
            │   │   ├── cart/
            │   │   ├── order/
            │   │   ├── authorization/
            │   │   └── operation/
            │   ├── exceptions/
            │   └── ports/
            │       ├── in/
            │       └── out/
            │
            └── infrastructure/
                ├── config/
                ├── database/
                └── security/
```

The concrete `adapters/` and most of `infrastructure/` are delivered in later phases
(persistence, REST, security). Phases 1–4 populate `domain/` and `infrastructure/config/`.

---

# Layer Responsibilities

## Application

Root package of the project. Contains the application entry point
(`NexusMarketApplication`) and organizes all architectural components. Dependency
composition happens here through Spring component scanning.

## Adapters

Adapters connect external technologies with the business domain. They translate
external requests into domain operations and transform domain objects into
technology-specific representations. The domain never communicates directly with
external systems.

### Input Adapters — `adapters/in/rest`

- Receive HTTP requests.
- Validate incoming data.
- Convert Request DTOs into Domain Models (via mappers).
- Invoke domain Input Ports.
- Convert domain results into Response DTOs.

Controllers must never implement business rules. **DTOs must never enter the Domain
layer.**

### Output Adapters — `adapters/out`

- **`persistence/mysql`** — relational persistence for users, catalog, inventory,
  carts and orders. Entities, Spring Data repositories, mappers, and port
  implementations.
- **`persistence/mongodb`** — append-only storage for `AuditLog` records.
- **`payment`** — implements `PaymentGatewayPort`. Hosts the payment **simulation**
  (probabilistic approval/rejection enabling buyer retries). The domain is unaware of
  how payment is resolved.
- **`notification`** — implements `NotificationPort` (email/SMS/push).

An output port does not necessarily correspond one-to-one with a physical table.

---

# Domain

The Domain layer is the core of the application. It contains all business rules and
must remain independent from any external technology.

No class inside the domain may depend on: Spring internals for business logic, JPA,
MongoDB, HTTP, REST, JSON, or SQL.

> Practical exception, aligned with the reference project: domain **services** are
> annotated with `@Service` and use constructor injection (Lombok
> `@RequiredArgsConstructor`) purely for wiring. They must still be fully unit-testable
> by direct instantiation, without a Spring context.

## Models — `domain/models`

Business entities. NexusMarket hierarchy:

```text
Person (abstract)
├── User
├── Buyer
└── Seller

Product (abstract)
├── PhysicalProduct
└── DigitalProduct

Category
ProductVariant
Warehouse
InventoryItem
InventoryMovement
Cart / CartItem
Order / OrderItem
Operation
AuditLog
```

Relationships are expressed through **object references**, never through primitive
identifier fields.

## Value Objects — `domain/valueobjects`

Immutable business concepts compared by value.

- **`DomainCatalog`** (abstract): `code`, `name`, `description`; equality by `code`.
  Controlled catalogs extend it: `UserRole`, `UserStatus`, `BuyerCommercialStatus`,
  `SellerStatus`, `WarehouseType`, `ProductType`, `ProductStatus`, `OrderStatus`,
  `CartStatus`, `InventoryMovementType`, `InventoryItemCondition`, `OperationType`.
- **Primitive wrappers**: `Money`, `Email`, `DocumentId`, `StockQuantity`.

## Enums — `domain/enums`

Fixed technical values without business metadata: `PaymentResult`,
`NotificationChannel`, `AuditSeverity`.

## Services — `domain/services/<subdomain>`

One class per use case, named verb + noun, exposing a single `execute(...)` method.
Services coordinate models, compose authorization services, and register every
significant action through the Operation & Audit subdomain.

## Ports — `domain/ports`

The domain owns all port interfaces.

- **Input Ports (`ports/in`)** — one interface per use case. Define *what the system
  can do*. Implemented by the corresponding domain service.
- **Output Ports (`ports/out`)** — dependencies the domain needs from the outside:
  repository ports, `PaymentGatewayPort`, `NotificationPort`, `OperationRepositoryPort`,
  `AuditLogRepositoryPort`, `PasswordServicePort`, `JwtServicePort`,
  `BusinessConfigurationPort`.

Port methods work with **Domain Models**, not DTOs, persistence entities, or bare
primitive identifiers.

## Exceptions — `domain/exceptions`

`DomainException` (base), `EntityNotFoundException`, `InvalidStatusTransitionException`,
`UnauthorizedOperationException`, `NegativeStockException`, `InvalidReservationException`,
`FinalizedOrderModificationException`, `DuplicateUserException`,
`PaymentRejectedException`.

---

# Infrastructure

Technical configuration only; no business logic.

- **`config`** — `JacksonConfig`, `GlobalExceptionHandler`, `ApiResponse` wrapper.
- **`database`** — MySQL and MongoDB connection configuration (later phase).
- **`security`** — JWT configuration, password encoder, authentication filters
  (later phase).

---

# Dependency Flow

```
REST Controller
      │
      ▼
Input Port  ──────────────┐
      │                   │
      ▼                   │
Domain Service            │  (implements)
      │                   │
      ▼                   │
Output Port               │
      │                   │
      ▼                   │
Persistence / Payment / Notification Adapter
      │
      ▼
External Resource
```

Every dependency points toward the Domain. The domain never depends on adapters or
infrastructure.

---

# Architectural Constraints

1. Business logic belongs exclusively to the Domain layer.
2. Controllers must not contain business rules.
3. DTOs must never enter the Domain layer.
4. Persistence entities must never be exposed through the API.
5. Communication between technologies and the Domain occurs only through Ports.
6. Adapters implement Ports but never define business rules.
7. Infrastructure depends on the Domain, never the opposite.
8. Every dependency must point toward the Domain.
9. Business entities must remain framework-independent.
10. The Domain must be fully testable without infrastructure.
11. Domain relationships must use Domain Models, not primitive identifiers.
12. Every significant business action must generate an `Operation` and an `AuditLog` record.
13. Externally configurable business parameters are read through `BusinessConfigurationPort`.
14. Each user has exactly one role (spec RG02); role-based access is enforced in the domain.
15. A finalized (`DELIVERED`) order is immutable (spec §11).
