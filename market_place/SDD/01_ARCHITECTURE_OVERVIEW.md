# 01. Architecture Overview

## Introduction
The **Market Place API** is built using **Hexagonal Architecture** (Ports and Adapters) combined with **Domain-Driven Design (DDD)** principles.

## Layer Definitions
The project follows a strict package structure:

### 1. Domain Layer (`domain/`)
The absolute core of the system.
- **Entities:** Objects with a distinct identity.
- **Aggregates:** A cluster of domain objects.
- **Value Objects:** Immutable objects.
- **Exceptions:** Business rule violations.
- **Repositories (Ports):** Output ports for the domain.
- **Services:** Business logic that doesn't fit within a single entity.
*Rule:* The Domain Layer **MUST NOT** depend on any other layer or framework (like Spring or JPA).

### 2. Application Layer (`application/`)
Orchestrates the execution of business use cases.
- **Input Ports:** Interfaces defining the use cases.
- **Output Ports:** Interfaces defining external dependencies needed by the application (e.g., mail).
- **Use Cases:** Implementation of the Input Ports.
- **DTOs:** Data Transfer Objects.
*Rule:* The Application Layer depends only on the Domain Layer.

### 3. Adapters Layer (`adapter/`)
Bridges the gap between the application/domain and the outside world.
- **In (`adapter/in/`):** REST Controllers, Schedulers.
- **Out (`adapter/out/`):** Persistence (JPA Entities, Repositories, Mappers, Persistence Adapters) and external services.

### 4. Configuration & Shared (`config/`, `shared/`)
- Spring configurations, Security, Global Exceptions, Jackson config.
