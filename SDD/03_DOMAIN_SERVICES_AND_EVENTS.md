# 03. Domain Services and Events

## Overview
While entities and aggregates encapsulate logic that belongs to a single object, the **Market Place** requires complex business rules that orchestrate multiple aggregates. These rules are encapsulated within Domain Services and communicated via Domain Events, maintaining the purity of the Hexagonal Architecture.

## Domain Services
Located in `domain.service`, these services handle cross-aggregate operations without depending on external infrastructure or databases.

### 1. `PaymentSimulationDomainService`
**Responsibility:** Simulates the interaction with a payment gateway.
**Business Rules:**
- Checks if the `Order` is in the `PENDING_PAYMENT` state.
- Simulates an 80% success rate.
- Randomly fails (20% chance) by throwing a `PaymentRejectedException`, forcing the user to retry the payment without discarding the entire cart.

### 2. `InventoryDomainService`
**Responsibility:** Manages stock levels and ensures strict traceability.
**Business Rules:**
- **Reservation:** Reduces available stock from an `InventoryItem`. If stock falls below zero, the `StockQuantity` Value Object strictly prevents it by throwing a `NegativeStockException`.
- **Traceability:** Generates an `InventoryMovement` entity for every action (`RESERVE`, `SALE`), creating an immutable audit trail of how stock moves through the warehouse.

### 3. `OrderCheckoutDomainService`
**Responsibility:** Orchestrates the transition of an `Order` from cart creation to payment and delivery.
**Business Rules:**
- Validates that an order is not empty before initiating checkout.
- Depending on the `ProductType`, dictates the post-payment flow:
  - **Digital Products:** Automatically transitions the order to `DELIVERED` upon successful payment.
  - **Physical Products:** Keeps the order as `PAID`, pending fulfillment by the logistics role.

## Domain Events
Located in `domain.event`, these events are generated when significant domain changes occur, allowing loosely coupled side-effects (like email notifications) to be triggered later in the Application layer.

- **`OrderCreatedEvent`:** Fired when a user initiates the checkout.
- **`OrderPaidEvent`:** Fired upon successful simulated payment.
- **`OrderPaymentFailedEvent`:** Fired when the simulated payment rejects the transaction.
- **`StockDepletedEvent`:** Fired when an inventory reservation brings the available stock to exactly zero.
