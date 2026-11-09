# NexusMarket API

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-brightgreen.svg)
![Architecture](https://img.shields.io/badge/Architecture-Hexagonal%20%7C%20DDD-blue.svg)
![Status](https://img.shields.io/badge/Status-Phases%201--7%20complete-brightgreen.svg)

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
| `user` | System identities: login, register, change status, consult |
| `buyer` | Buyer self-registration, profile maintenance, commercial status, consultation |
| `seller` | Seller onboarding (with first warehouse) and consultation |
| `warehouse` | Marketplace / seller storage locations |
| `catalog` | Publish and maintain products, variants, status, and portfolio consultation |
| `inventory` | Distributed stock: entry, reservation, release, adjustment, return (spec §11) |
| `cart` | The buyer's single active shopping cart |
| `order` | Checkout, payment, dispatch (with `SALE_EXIT` logging), delivery, consultation, expired reservation release |
| `authorization` | Role / status / ownership guards (internal collaborators) |
| `operation` | Operation + AuditLog registration and consultation |

**Order lifecycle** (spec Domain 7):

```
CART → PENDING_PAYMENT → PAID → DISPATCHED → DELIVERED
                          └──────────────────► DELIVERED   (digital-only orders)
```

A rejected payment leaves the order in `PENDING_PAYMENT` so the buyer can retry;
orders that remain unpaid past the configured window have their reserved physical stock
automatically released by `OrderExpirationScheduler`.

---

## Technologies Used

- **Core:** Java 17, Spring Boot 4.1
- **Persistence:** Spring Data JPA + MySQL (transactional data), Spring Data MongoDB (audit trail)
- **Security:** Spring Security, Stateless JWT (`jjwt`)
- **Validation:** Jakarta Bean Validation
- **API Documentation:** OpenAPI 3 / Swagger UI (`springdoc-openapi`)
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
- **`application.adapters`** — inbound REST controllers (`in.rest`), scheduled tasks
  (`in.scheduler`), and outbound adapters (`out.persistence.mysql`,
  `out.persistence.mongodb`, `out.payment`, `out.notification`).
- **`application.infrastructure`** — `config` (Jackson, OpenAPI, exception handling,
  response wrapper, business configuration, initial Admin `DataSeeder`) and `security`
  (BCrypt, JJWT, stateless `JwtAuthenticationFilter`, `SecurityConfig`).

```
REST Controller / Scheduler ──► Input Port ──► Domain Service ──► Output Port ──► Adapter ──► DB / gateway
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
│   ├── exceptions/      # DomainException + 9 specific exceptions
│   ├── services/        # one class per use case, grouped by subdomain
│   │   ├── user/  buyer/  seller/  warehouse/  catalog/
│   │   ├── inventory/  cart/  order/
│   │   └── authorization/  operation/  identity/
│   └── ports/
│       ├── in/          # use-case interfaces (35)
│       └── out/         # repository + service ports (16)
├── adapters/
│   ├── in/
│   │   ├── rest/        # controllers (10), requests (12), responses (13), mappers (9)
│   │   └── scheduler/   # OrderExpirationScheduler
│   └── out/
│       ├── persistence/
│       │   ├── mysql/   # entities (14), repositories (11), mappers (10), adapters (10)
│       │   └── mongodb/ # AuditLogDocument, AuditLogMongoRepository, mapper, adapter
│       ├── payment/     # SimulatedPaymentAdapter
│       └── notification/# EmailNotificationAdapter
└── infrastructure/
    ├── config/          # JacksonConfig, OpenApiConfig, GlobalExceptionHandler, ApiResponse, PropertiesConfigurationAdapter, DataSeeder
    └── security/        # BCryptPasswordAdapter, JjwtAdapter, JwtAuthenticationFilter, AuthenticatedUserResolver, SecurityConfig
```

---

## Current Progress

The project follows a phased implementation plan
([`IMPLEMENTATION_PLAN.md`](IMPLEMENTATION_PLAN.md)) and was realigned to the reference
conventions in [`ALIGNMENT_PLAN.md`](ALIGNMENT_PLAN.md).

- [x] **Phase 1** — Base structure, build, `application`-rooted packages, architecture SDD.
- [x] **Phase 2** — Domain model: `DomainCatalog` value objects, entity hierarchy, exceptions.
- [x] **Phase 3** — Domain services (per subdomain) + Operation/Audit + authorization.
- [x] **Phase 4** — Input / Output ports (35 in, 16 out); legacy `dto` / `usecase` / `port` layer retired.
- [x] **Phase 5** — Persistence adapters (MySQL + MongoDB), payment simulation, email notification & configuration adapters.
- [x] **Phase 6** — REST controllers (10) + Spring Security (JWT) + OpenAPI/Swagger + `@SpringBootTest`.
- [x] **Phase 7** — Schedulers (`OrderExpirationScheduler`), initial Admin `DataSeeder`, end-to-end integration tests, and complete SDD documentation.

All 7 phases build green (`./mvnw clean test`).

---

## Getting Started

### Prerequisites
- **Java 17** or higher.
- The bundled Maven wrapper (`./mvnw`) — no local Maven install required.
- **MySQL** (`market_place_db`) and **MongoDB** (`market_place_audit_db`) for runtime execution.

### Build & Run

```bash
./mvnw clean compile
./mvnw spring-boot:run
```

On startup, `DataSeeder` automatically creates the initial `ADMIN` account (`admin` / `admin123`, configurable via `app.seed.admin.*` in `application.properties`) if it does not already exist.
Swagger UI is available at `http://localhost:8080/swagger-ui.html`.

---

## Testing

The domain, persistence adapters, security infrastructure, REST controllers, scheduler, seeder, end-to-end multi-role lifecycle, and full Spring application context are verified via `./mvnw test`:

```bash
./mvnw test
```

```
Tests run: 80, Failures: 0, Errors: 0, Skipped: 0
```

---

## Documentation (SDD)

The Software Design Document lives in [`SDD/`](SDD/).

- **[Software Architecture](SDD/Software%20Architecture/Software%20Architecture.md)** — layers, dependency rules, constraints.
- **[Code Walkthrough & Role Flows](SDD/Code%20Walkthrough%20and%20Role%20Flows.md)** — how the code calls together end to end, the login flow, and a step-by-step journey per role.
- **[Persistence and Output Adapters](SDD/adapters/Persistence%20and%20Output%20Adapters.md)** — JPA entities, Spring Data repositories, mappers, MySQL & MongoDB adapters, payment simulation, email notifications, and configuration adapter.
- **[REST and Security Adapters](SDD/adapters/REST%20and%20Security%20Adapters.md)** — JWT authentication, `SecurityConfig`, REST controllers, Request/Response DTOs, REST mappers, and exception mapping.
- **[Schedulers and Bootstrap Configuration](SDD/adapters/Schedulers%20and%20Bootstrap.md)** — `OrderExpirationScheduler`, `ReleaseExpiredOrdersService`, and idempotent `DataSeeder`.

**Domain (`SDD/domain/`)**

- [Domain Model](SDD/domain/Domain%20Model.md) — entity hierarchy, relationships, lifecycle.
- [Domain Value Objects](SDD/domain/Domain%20Value%20Objects.md) — `DomainCatalog`, catalogs, primitive wrappers.
- [Domain Services](SDD/domain/Domain%20Services.md) — high-level catalogue + shared principles.
- [Output Ports](SDD/domain/Output%20Ports.md) — 16 ports: responsibility, signatures, consumers, adapter mapping.
- [Input Ports](SDD/domain/Input%20Ports.md) — 35 use-case contracts.

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
