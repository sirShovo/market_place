# NexusMarket API

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-brightgreen.svg)
![Architecture](https://img.shields.io/badge/Architecture-Hexagonal%20%7C%20DDD-blue.svg)
![Status](https://img.shields.io/badge/Status-Domain%20alignment%20(phases%201--4)-yellow.svg)

A RESTful API for **NexusMarket**, a digital and physical marketplace that acts as a
commercial intermediary between buyers and sellers. Built with **Spring Boot**,
**Hexagonal Architecture (Ports and Adapters)** and **Domain-Driven Design (DDD)**.

---

## Table of Contents
1. [About the Project](#about-the-project)
2. [Technologies Used](#technologies-used)
3. [Architecture & Design](#architecture--design)
4. [Current Progress](#current-progress)
5. [Getting Started](#getting-started)
6. [Testing](#testing)
7. [Documentation (SDD)](#documentation-sdd)

---

## About the Project

NexusMarket manages the full e-commerce lifecycle: user administration, seller and
warehouse onboarding, product catalog, distributed inventory, shopping cart, and the
complete order lifecycle. The functional specification is kept in
[`SPECS.md`](SPECS.md).

**Key business rules enforced by the domain:**
- **Physical vs. digital fulfillment** — physical products require inventory and
  dispatch; digital products are delivered immediately after payment.
- **Strict inventory control** — no negative stock; damaged inventory cannot be
  reserved; every movement (entry, reservation, sale exit, adjustment, return) is
  traceable.
- **Single role per user** (spec RG02) and role-based restrictions — e.g. sellers are
  onboarded by an Administrator, never self-registered.
- **Finalized orders are immutable** (spec §11).
- **Operation & Audit trail** — every significant business action produces an
  `Operation` and an append-only `AuditLog` record.

---

## Technologies Used

- **Core:** Java 17, Spring Boot 4.1
- **Persistence:** Spring Data JPA + MySQL (transactional data), MongoDB (audit trail)
- **Security:** Spring Security, JWT
- **Validation:** Jakarta Bean Validation
- **API Documentation:** OpenAPI (Swagger)
- **Build Tool:** Maven
- **Boilerplate:** Lombok

---

## Architecture & Design

Hexagonal Architecture + DDD, rooted at the `application` package:

- **`application.domain`** — pure business core: `models`, `valueobjects`, `enums`,
  `services` (one class per use case, grouped by subdomain), `exceptions`, and
  `ports` (`in` / `out`). No dependency on persistence, HTTP or JSON.
- **`application.adapters`** — inbound REST controllers and outbound adapters
  (MySQL / MongoDB persistence, payment gateway, notifications). *Later phases.*
- **`application.infrastructure`** — `config` (Jackson, exception handling, response
  wrapper), `database`, `security`. Technical wiring only.

Full detail in [`SDD/Software Architecture/Software Architecture.md`](SDD/Software%20Architecture/Software%20Architecture.md).

---

## Current Progress

The project follows a phased implementation plan
([`IMPLEMENTATION_PLAN.md`](IMPLEMENTATION_PLAN.md)). It is currently undergoing an
alignment pass so phases 1–4 match the reference conventions and the SDD is brought to
full detail — see [`ALIGNMENT_PLAN.md`](ALIGNMENT_PLAN.md).

- [x] **Phase 1:** Base structure, build, `application`-rooted packages, architecture SDD.
- [x] **Phase 2:** Domain model — `DomainCatalog` value objects, entity hierarchy, exceptions.
- [x] **Phase 3:** Domain services (per subdomain) + Operation/Audit + authorization.
- [x] **Phase 4:** Input/Output ports; retire the legacy `dto` / `usecase` / `port` layer.
- [ ] **Phase 5:** Persistence adapters (MySQL + MongoDB), payment simulation adapter.
- [ ] **Phase 6:** REST controllers + Spring Security.
- [ ] **Phase 7:** Schedulers and final refinements.

---

## Getting Started

### Prerequisites
- **Java 17** or higher.
- **Maven** (or the bundled `mvnw` wrapper).
- **MySQL** with a database named `market_place_db` (only required from Phase 5 onward).

### Compile

```bash
./mvnw clean compile
```

During phases 1–4 the API endpoints are not yet exposed; the domain compiles and is
unit-testable in isolation.

---

## Testing

The domain is testable without a Spring context or a database:

```bash
./mvnw test
```

> Note: `@SpringBootTest` context tests are disabled until Phase 5, when the output
> ports get their adapters. Until then, domain services are covered by plain unit
> tests (direct instantiation).

---

## Documentation (SDD)

Architectural decisions and layer documentation live in the [`SDD/`](SDD/) directory:

- **[Software Architecture](SDD/Software%20Architecture/Software%20Architecture.md)** — layers, dependency rules, constraints.
- **`SDD/domain/`** — Domain Model, Domain Value Objects, Domain Services, Output Ports, Input Ports.
- **`SDD/domain/services/`** — one document per service subdomain.

*(Populated progressively as each phase lands.)*
