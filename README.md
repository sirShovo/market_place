# NexusMarket API

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-brightgreen.svg)
![Architecture](https://img.shields.io/badge/Architecture-Hexagonal%20%7C%20DDD-blue.svg)
![Status](https://img.shields.io/badge/Status-Phases%201--4%20complete-yellow.svg)

A RESTful API for **NexusMarket**, a digital and physical marketplace that acts as a
commercial intermediary between buyers and sellers. Built with **Spring Boot**,
**Hexagonal Architecture (Ports and Adapters)** and **Domain-Driven Design (DDD)**, so
the business rules are fully isolated from frameworks, databases and transport.

---

## Table of Contents
1. [About the Project](#about-the-project)
2. [Domain Overview](#domain-overview)
3. [Technologies Used](#technologies-used)
4. [Architecture & Design](#architecture--design)
5. [Project Structure](#project-structure)
6. [Current Progress](#current-progress)
7. [Getting Started](#getting-started)
8. [Testing](#testing)
9. [Documentation (SDD)](#documentation-sdd)

---

## About the Project

NexusMarket manages the full e-commerce lifecycle: user administration, seller and
warehouse onboarding, product catalog, distributed inventory, shopping cart, and the
complete order lifecycle. The functional specification is kept in
[`SPECS.md`](SPECS.md).

**Key business rules enforced by the domain**

- **One role per user** (spec RG02); every operation runs on behalf of an authenticated,
  active user (RG01); no participant manages data outside its role (RG03).
- **Sellers are onboarded by an Administrator**, together with their first warehouse —
  never self-registered. Buyers do self-register.
- **Physical vs. digital fulfillment** — physical products need inventory and dispatch;
  digital products are delivered immediately after payment.
- **Strict inventory control** — inventory is always bound to a `(product, warehouse)`
  pair; stock can never go negative; damaged stock cannot be reserved; every movement
  (entry, reservation, sale exit, adjustment, return) is traceable.
- **Unique document ID and e-mail** across the whole platform (spec §11).
- **Finalized (`DELIVERED`) orders are immutable** (spec §11).
- **Operation & Audit trail** — every significant business action produces an
  `Operation` and an append-only `AuditLog` record.

---

## Domain Overview

**Participant roles:** `BUYER`, `SELLER`, `LOGISTICS_OPERATOR`, `ADMIN`, `SUPERVISOR`.

**Subdomains** (`application.domain.services.<name>`, one service class per use case):

| Subdomain | Responsibility |
| --------- | -------------- |
| `user` | System identities: register, change status, consult |
| `buyer` | Buyer self-registration and profile |
| `seller` | Seller onboarding (with first warehouse) |
| `warehouse` | Marketplace / seller storage locations |
| `catalog` | Publish and maintain products, variants and status |
| `inventory` | Distributed stock: entry, reservation, release, adjustment (spec §11) |
| `cart` | The buyer's single active shopping cart |
| `order` | Checkout, payment, dispatch, delivery |
| `authorization` | Role / status / ownership guards (internal collaborators) |
| `operation` | Operation + AuditLog registration and consultation |

**Order lifecycle** (spec Domain 7):

```
CART → PENDING_PAYMENT → PAID → DISPATCHED → DELIVERED
                          └──────────────────► DELIVERED   (digital-only orders)
```

A rejected payment leaves the order in `PENDING_PAYMENT` so the buyer can retry.

---

## Technologies Used

- **Core:** Java 17, Spring Boot 4.1
- **Persistence:** Spring Data JPA + MySQL (transactional data), MongoDB (audit trail) — *from Phase 5*
- **Security:** Spring Security, JWT — *from Phase 6*
- **Validation:** Jakarta Bean Validation
- **API Documentation:** OpenAPI (Swagger) — *from Phase 6*
- **Build Tool:** Maven (wrapper included)
- **Boilerplate:** Lombok

---

## Architecture & Design

Hexagonal Architecture + DDD, rooted at the `application` package. Every dependency
points **toward** the domain.

- **`application.domain`** — pure business core: `models`, `valueobjects`, `enums`,
  `services` (one class per use case, grouped by subdomain), `exceptions`, and
  `ports` (`in` / `out`). No dependency on JPA, HTTP or JSON. Domain services are
  annotated `@Service` purely for wiring and stay unit-testable without a Spring
  context.
- **`application.adapters`** — inbound REST controllers and outbound adapters
  (MySQL / MongoDB persistence, payment gateway, notifications). *Phases 5–6.*
- **`application.infrastructure`** — `config` (Jackson, exception handling, response
  wrapper), `database`, `security`. Technical wiring only.

```
REST Controller ──► Input Port ──► Domain Service ──► Output Port ──► Adapter ──► DB / gateway
```

Full detail in
[`SDD/Software Architecture/Software Architecture.md`](SDD/Software%20Architecture/Software%20Architecture.md).

---

## Project Structure

```
src/main/java/application/
├── NexusMarketApplication.java
├── domain/
│   ├── models/          # Person→User/Buyer/Seller, Product→Physical/Digital,
│   │                    #   Category, ProductVariant, Warehouse, InventoryItem,
│   │                    #   InventoryMovement, Cart/CartItem, Order/OrderItem,
│   │                    #   Operation, AuditLog, Notification, AuditableEntity
│   ├── valueobjects/    # DomainCatalog + catalogs (UserRole, OrderStatus, …);
│   │                    #   Money, Email, DocumentId, StockQuantity (records)
│   ├── enums/           # PaymentResult, NotificationChannel, AuditSeverity
│   ├── exceptions/      # DomainException + 8 specific exceptions
│   ├── services/        # one class per use case, grouped by subdomain
│   │   ├── user/  buyer/  seller/  warehouse/  catalog/
│   │   ├── inventory/  cart/  order/
│   │   └── authorization/  operation/
│   └── ports/
│       ├── in/          # use-case interfaces (28)
│       └── out/         # repository + service ports (16)
└── infrastructure/
    └── config/          # JacksonConfig, GlobalExceptionHandler, ApiResponse

src/test/java/application/domain/
├── models/OrderTest.java
├── services/user/RegisterUserServiceTest.java
├── services/inventory/ReserveInventoryServiceTest.java
└── support/Fakes.java     # hand-written test-double ports
```

---

## Current Progress

The project follows a phased implementation plan
([`IMPLEMENTATION_PLAN.md`](IMPLEMENTATION_PLAN.md)) and was realigned to the reference
conventions in [`ALIGNMENT_PLAN.md`](ALIGNMENT_PLAN.md).

- [x] **Phase 1** — Base structure, build, `application`-rooted packages, architecture SDD.
- [x] **Phase 2** — Domain model: `DomainCatalog` value objects, entity hierarchy, exceptions.
- [x] **Phase 3** — Domain services (per subdomain) + Operation/Audit + authorization.
- [x] **Phase 4** — Input / Output ports; legacy `dto` / `usecase` / `port` layer retired.
- [ ] **Phase 5** — Persistence adapters (MySQL + MongoDB), payment simulation adapter.
- [ ] **Phase 6** — REST controllers + Spring Security (JWT) + Swagger.
- [ ] **Phase 7** — Schedulers (unpaid-order expiration) and final refinements.

Phases 1–4 build green (`./mvnw clean test`).

---

## Getting Started

### Prerequisites
- **Java 17** or higher.
- The bundled Maven wrapper (`./mvnw`) — no local Maven install required.
- **MySQL** with a database named `market_place_db` — only needed from **Phase 5**.

### Build

```bash
./mvnw clean compile
```

During phases 1–4 the API endpoints are not exposed yet; the domain compiles and is
unit-testable in isolation. `./mvnw spring-boot:run` and the Swagger UI arrive in
Phase 6.

---

## Testing

The domain is testable without a Spring context or a database:

```bash
./mvnw test
```

Current coverage (plain unit tests, direct instantiation with test-double ports):

| Test | What it checks |
| ---- | -------------- |
| `OrderTest` | total = Σ subtotals; valid lifecycle; illegal transition → `InvalidStatusTransitionException`; finalized order immutable |
| `RegisterUserServiceTest` | `ADMIN` registers `SELLER` + password encrypted; non-`ADMIN` rejected; duplicate document rejected |
| `ReserveInventoryServiceTest` | reservation decrements stock + logs movement; missing / `DAMAGED` inventory rejected; over-reservation → `NegativeStockException` |

```
Tests run: 12, Failures: 0, Errors: 0, Skipped: 1
```

> `NexusMarketApplicationTests` (`@SpringBootTest`) is **`@Disabled` until Phase 5**:
> the domain services are `@Service` beans that depend on output ports whose adapters
> do not exist yet, so the full context cannot be built.

---

## Documentation (SDD)

The Software Design Document lives in [`SDD/`](SDD/).

- **[Software Architecture](SDD/Software%20Architecture/Software%20Architecture.md)** — layers, dependency rules, constraints.

**Domain (`SDD/domain/`)**

- [Domain Model](SDD/domain/Domain%20Model.md) — entity hierarchy, relationships, lifecycle.
- [Domain Value Objects](SDD/domain/Domain%20Value%20Objects.md) — `DomainCatalog`, catalogs, primitive wrappers.
- [Domain Services](SDD/domain/Domain%20Services.md) — high-level catalogue + shared principles.
- [Output Ports](SDD/domain/Output%20Ports.md) — 16 ports: responsibility, signatures, consumers, adapter mapping.
- [Input Ports](SDD/domain/Input%20Ports.md) — 28 use-case contracts.

**Per-subdomain service specs (`SDD/domain/services/`)**

[user](SDD/domain/services/user-services.md) ·
[buyer](SDD/domain/services/buyer-services.md) ·
[seller](SDD/domain/services/seller-services.md) ·
[warehouse](SDD/domain/services/warehouse-services.md) ·
[catalog](SDD/domain/services/catalog-services.md) ·
[inventory](SDD/domain/services/inventory-services.md) ·
[cart](SDD/domain/services/cart-services.md) ·
[order](SDD/domain/services/order-services.md) ·
[authorization](SDD/domain/services/authorization-services.md) ·
[operation & audit](SDD/domain/services/operation-audit-services.md)
