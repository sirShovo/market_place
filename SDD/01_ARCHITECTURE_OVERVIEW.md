# 01. Architecture Overview

## Introduction
The **NexusMarket API** is built using **Hexagonal Architecture** (also known as Ports and Adapters) combined with **Domain-Driven Design (DDD)** principles. This approach ensures that the core business logic is completely isolated from external concerns such as frameworks, databases, or user interfaces.

## Layer Definitions

The project follows a strict package structure designed to enforce dependency rules:

### 1. Domain Layer (`domain/`)
The absolute core of the system. It contains the business concepts, rules, and logic.
- **Entities:** Objects with a distinct identity that persists over time (e.g., `User`, `Category`).
- **Aggregates:** A cluster of domain objects that can be treated as a single unit for data changes (e.g., `Product`, `Order`).
- **Value Objects:** Immutable objects that describe characteristics or attributes but have no conceptual identity (e.g., `Email`, `Role`).
- **Domain Exceptions:** Business rule violations (e.g., `NegativeStockException`).
- **Domain Services:** Business logic that doesn't naturally fit within a single entity.
- **Output Ports (Repositories):** Java Interfaces defining how the domain interacts with data storage.

*Rule:* The Domain Layer **MUST NOT** depend on any other layer or framework (like Spring or JPA). It relies purely on Java.

### 2. Application Layer (`application/`)
This layer orchestrates the execution of business use cases. It does not contain business rules but rather coordinates the domain objects to fulfill a request.
- **Input Ports:** Interfaces defining the use cases.
- **Use Cases (Services):** Implementation of the Input Ports.
- **DTOs:** Data Transfer Objects used to receive data from and return data to the adapters.

*Rule:* The Application Layer can depend on the Domain Layer but **MUST NOT** depend on the Adapters (Web or Persistence).

### 3. Adapters Layer (`adapter/`)
This layer bridges the gap between the application/domain and the outside world.

#### Driving Adapters (`adapter/in/web/`)
- **REST Controllers:** Receive HTTP requests, map them to DTOs, and invoke the Application Layer's Input Ports.

#### Driven Adapters (`adapter/out/persistence/`)
- **Persistence Adapters:** Implement the Output Ports (Repository interfaces) defined in the Domain Layer.
- **JPA Entities & Spring Data Repositories:** Framework-specific mechanisms for storing data.
- **Mappers:** Responsible for converting Domain Entities to JPA Entities and vice-versa.

### 4. Configuration & Shared (`config/`, `shared/`)
- **Config:** Spring Boot configurations, Security (JWT, Filters), and Global Exception Handling.
- **Shared:** Utility classes and helpers used across different non-domain layers.

## Benefits for NexusMarket
- **Testability:** Core business rules (like checking inventory or verifying roles) can be unit-tested without loading the Spring context or a database.
- **Maintainability:** Changes in the database schema (JPA) do not impact the business logic, as they are decoupled by mappers.
- **Flexibility:** Easy to replace the web interface or the database technology in the future.
