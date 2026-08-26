# 02. Domain Layer

## Value Objects
- `Money`: Represents monetary values, prevents negative amounts.
- `Email` & `DocumentId`: Immutable identifiers for users.
- `StockQuantity`: Wraps inventory integer values ensuring no negative stocks.
- `Enums`: `UserRole`, `ProductType`, `OrderStatus`, `MovementType`, etc.

## Entities
- `User` & `BuyerProfile`: Manage identity and authentication boundaries.
- `Warehouse`: Defines storage locations managed by Admins or Sellers.
- `InventoryItem` & `InventoryMovement`: A transactional audit log ensuring strict adherence to the non-negative rule. 
- `OrderItem`, `AuditLog`, `Category`.

## Aggregates
- `Product`: Root for catalog items, physically or digitally represented.
- `Order`: Encapsulates cart rules, item addition, and lifecycle status changes.

## Exceptions
- `DomainValidationException`, `NegativeStockException`, `PaymentRejectedException`, etc., thrown when business rules are violated.
