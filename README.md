# Market Place API

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)
![Architecture](https://img.shields.io/badge/Architecture-Hexagonal%20%7C%20DDD-blue.svg)
![Status](https://img.shields.io/badge/Status-In%20Development%20(Phase%203)-yellow.svg)

A robust, enterprise-grade RESTful API for a digital and physical Market Place. Built with **Spring Boot**, **Hexagonal Architecture (Ports and Adapters)**, and **Domain-Driven Design (DDD)** principles to ensure high maintainability, testability, and strict business rule enforcement.

---

## Table of Contents
1. [About the Project](#about-the-project)
2. [Technologies Used](#technologies-used)
3. [Architecture & Design](#architecture--design)
4. [Current Progress](#current-progress)
5. [Getting Started](#getting-started)
6. [Usage](#usage)
7. [Documentation (SDD)](#documentation-sdd)

---

## About the Project

The Market Place API is designed to manage the entire lifecycle of e-commerce operations. It supports a multi-role ecosystem including Buyers, Sellers, Logistics Operators, Administrators, and Supervisors. 

**Key Business Rules Enforced by the Domain:**
- **Physical vs. Digital Flows:** Differentiates fulfillment logistics (Physical items require shipping, Digital items are delivered instantly).
- **Strict Inventory Control:** Prevents negative stock and ensures full auditability through transactional `InventoryMovement` logs.
- **Payment Simulation:** Incorporates realistic fault-tolerant checkout workflows with simulated payment rejections.
- **Role-Based Restrictions:** For example, Sellers cannot self-register and must be onboarded by Administrators.

---

## Technologies Used

- **Core:** Java 17, Spring Boot 3.x
- **Persistence:** Spring Data JPA, Hibernate, MySQL (with H2 for testing)
- **Security:** Spring Security, JWT (JSON Web Tokens)
- **Validation:** Jakarta Bean Validation (Hibernate Validator)
- **API Documentation:** OpenAPI (Swagger)
- **Build Tool:** Maven

---

## Architecture & Design

This project strictly adheres to **Hexagonal Architecture** and **Domain-Driven Design**. The application is segregated into decoupled layers:

- **Domain (`domain/`):** Pure Java. Contains Entities, Aggregates (`Product`, `Order`), Value Objects (`Money`, `StockQuantity`), Exceptions, and Domain Services. Has **zero** dependencies on Spring or JPA.
- **Application (`application/`):** Contains Use Cases, Input/Output Ports, and DTOs. Orchestrates domain logic.
- **Adapters (`adapter/`):** 
  - *Inbound:* REST Controllers handling HTTP requests.
  - *Outbound:* Persistence Mappers, JPA Entities, and third-party integrations (e.g., Email service).
- **Configuration (`config/`):** Spring Boot setup, Security filters, and Global Exception Handlers.

---

## Current Progress

The project is currently under active development and follows a phased implementation plan. 

- [x] **Phase 1:** Project Scaffolding, Spring Boot Setup, Global Exception Handling.
- [x] **Phase 2:** Domain Modeling (Entities, Value Objects, Aggregates, Domain Repositories).
- [x] **Phase 3:** Domain Services & Events (Payment Simulation, Inventory Management, Order Checkout).
- [ ] **Phase 4:** Application Layer (Use Cases, Ports, DTOs). *(Next)*
- [ ] **Phase 5:** Infrastructure Adapters (JPA Persistence).
- [ ] **Phase 6:** Web Adapters (REST Controllers & Security).
- [ ] **Phase 7:** Schedulers & Refinements.

---

## Getting Started

### Prerequisites
- **Java 17** or higher installed.
- **Maven** (optional, you can use the included `mvnw` wrapper).
- **MySQL** instance running (requires a database named `market_place_db`).

### Installation & Compilation

1. **Clone the repository:**
   ```bash
   git clone <your-repository-url>
   cd market_place/market_place
   ```

2. **Configure the Database:**
   Update the `src/main/resources/application.properties` file with your MySQL credentials:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/market_place_db?useSSL=false&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```

3. **Compile the project:**
   Since we are in Phase 3 (Domain Layer), the project compiles perfectly but the API endpoints are not yet exposed. You can verify the integrity of the architecture by running:
   ```bash
   ./mvnw clean compile
   ```

---

## Usage

At the current development stage, the core business logic is encapsulated in the `domain` package. 
- You can explore `src/main/java/domain/model/aggregate/Order.java` to see how checkout constraints are applied.
- Look at `src/main/java/domain/service/InventoryDomainService.java` to see how transactional movements (`RESERVE`, `SALE`) are logged purely via Domain Services without Database coupling.

Once **Phase 6** is completed, you will be able to run the application using `./mvnw spring-boot:run` and access the Swagger UI at `http://localhost:8080/swagger-ui.html`.

---

## Documentation (SDD)

Detailed architectural decisions and layer-specific documentation are maintained in the Software Design Document (`SDD/`) directory.

- **[01. Architecture Overview](market_place/SDD/01_ARCHITECTURE_OVERVIEW.md):** Deep dive into the Hexagonal layers and dependency rules.
- **[02. Domain Layer](market_place/SDD/02_DOMAIN_LAYER.md):** Documentation of the business aggregates, entities, and value objects.

*(More documents will be added as implementation phases progress).*
