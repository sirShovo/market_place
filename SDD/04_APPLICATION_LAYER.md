# 04. Application Layer

## Overview
The Application Layer sits outside the Domain Layer in Hexagonal Architecture. It orchestrates the domain objects (Entities, Aggregates, Domain Services) to perform business use cases. It acts as a strict boundary, preventing domain logic from leaking into the Web or Persistence layers.

## Key Components

### 1. DTOs (Data Transfer Objects)
Located in `application.dto`.
- **Purpose:** To prevent exposing the rich Domain Entities and Aggregates directly to the outside world (like REST controllers).
- **Implementation:** Implemented as Java `record` classes (e.g., `UserDto`, `OrderDto`) to ensure immutability and concise syntax. They only contain the primitive/wrapper fields necessary for the specific use case, decoupling the internal domain shape from the API response shape.

### 2. Input Ports
Located in `application.port.input`.
- **Purpose:** Define the interfaces (use cases) that the outer layer (REST Controllers) can invoke. This applies the Dependency Inversion Principle, ensuring the web layer depends on the application layer's contract, not its concrete implementation.
- **Examples:** `UserInputPort`, `OrderInputPort`.

### 3. Output Ports
Located in `application.port.output`.
- **Purpose:** Define interfaces that the application uses to communicate with external systems (like databases, email providers) without knowing their concrete implementations.
- **Note on Repositories:** In Phase 2, we defined our primary Output Ports for persistence inside the `domain.repository` package (e.g., `UserRepository`). This is a common and valid DDD practice where the domain dictates its persistence contract. Therefore, the Application Layer directly uses these domain repositories. We introduced `EmailNotificationPort` here specifically for cross-cutting application concerns like sending emails, which aren't strictly domain-persistence related.

### 4. Use Cases
Located in `application.usecase`.
- **Purpose:** The concrete implementations of the Input Ports.
- **Responsibilities:**
  1. Receive data (often primitives or DTOs) from the Input Port.
  2. Load the relevant Aggregates/Entities using Output Ports (Repositories).
  3. Invoke Domain Services or Aggregate methods to apply business rules.
  4. Save the modified state back via Output Ports.
  5. Return a DTO back to the caller.
- **Example Flow (`OrderUseCase.checkoutOrder`):** Retrieves the `Order` from the repository, passes it to the `OrderCheckoutDomainService` (which evaluates rules, invokes the payment simulation, and updates the status), and then saves the updated order before returning an `OrderDto`.
