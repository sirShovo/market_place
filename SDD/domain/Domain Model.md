# Domain Model

## Introduction

The Domain Model represents the core business entities of **NexusMarket**, the
marketplace described in [`SPECS.md`](../../SPECS.md). Entities encapsulate the
business rules, data, relationships and lifecycle concepts of the specification.

The model follows Object-Oriented and Domain-Driven Design principles:

* Inheritance is used for genuine domain specialization (`Person`, `Product`).
* Relationships are expressed with **object references**, never with primitive
  identifier fields (`Order.buyer : Buyer`, not `Order.buyerId : Long`).
* Every significant business action produces an `Operation`, which is recorded in an
  append-only `AuditLog`.

---

# Domain Class Hierarchy

```text
Person (abstract)
├── User          (system identity: authentication + access status)
├── Buyer         (commercial participant that places orders)
└── Seller        (participant that publishes products; onboarded by an Admin)

Product (abstract)
├── PhysicalProduct   (requires inventory and dispatch)
└── DigitalProduct    (instant delivery after payment)

Category
ProductVariant
Warehouse
InventoryItem
InventoryMovement
Cart / CartItem
Order / OrderItem
Operation
AuditLog

AuditableEntity (interface)  ← implemented by User, Seller, Warehouse, Product,
                               InventoryItem, Cart, Order
```

---

# Domain Relationships

```text
Person ──< role : UserRole >

User
  └── (a User has exactly one role; internal roles LOGISTICS_OPERATOR / ADMIN /
       SUPERVISOR are represented by a plain User)

Seller
  ├── onboardedBy ─────────> User (role ADMIN)
  └── warehouses ──────────> List<Warehouse>   (type SELLER)

Warehouse
  └── owner ───────────────> Seller?           (null when type == MARKETPLACE)

Product
  ├── seller ──────────────> Seller
  ├── category ────────────> Category
  └── variants ────────────> List<ProductVariant>

InventoryItem
  ├── product ─────────────> Product
  └── warehouse ───────────> Warehouse

InventoryMovement
  ├── inventoryItem ───────> InventoryItem
  └── performedBy ─────────> User

Cart
  ├── buyer ───────────────> Buyer
  └── items ───────────────> List<CartItem> ──> (product, variant?, quantity)

Order
  ├── buyer ───────────────> Buyer
  └── items ───────────────> List<OrderItem> ─> (product, variant?, quantity, unitPrice)

Operation
  ├── performedBy ─────────> User
  └── affectedEntity ──────> AuditableEntity

AuditLog
  ├── performedBy ─────────> User
  ├── userRole ────────────> UserRole   (role held when the action was performed)
  └── affectedEntity ──────> AuditableEntity
```

---

# Entities

## Person (abstract)

Common identity and contact information shared by every participant.

| Attribute      | Type       | Notes                                             |
| -------------- | ---------- | ------------------------------------------------- |
| identification | DocumentId | Unique across the platform (spec §11).            |
| fullName       | String     | Not empty (spec Domain 1).                        |
| email          | Email      | Unique across the platform (spec §11).            |
| phoneNumber    | String     | Optional.                                         |
| address        | String     | Optional.                                         |
| role           | UserRole   | Exactly one role per participant (spec RG02).     |

Cannot be instantiated directly.

## User

System identity used for authentication and authorization.

**Inherits:** `Person`

| Attribute | Type       | Notes                                        |
| --------- | ---------- | -------------------------------------------- |
| userId    | Integer    | Internal identifier.                          |
| username  | String     | Login name.                                  |
| password  | String     | Password hash (never the plain value).       |
| status    | UserStatus | System-access status (ACTIVE/BLOCKED/…).     |

A `User` with role `LOGISTICS_OPERATOR`, `ADMIN` or `SUPERVISOR` has no extra profile.

## Buyer

Commercial participant that browses the catalog and places orders (spec Domain 2).

**Inherits:** `Person` (role `BUYER`)

| Attribute           | Type                  | Notes                                    |
| ------------------- | --------------------- | ---------------------------------------- |
| commercialStatus    | BuyerCommercialStatus | Ability to purchase.                     |
| mainAddress         | String                | Required delivery address.               |
| additionalAddresses | List\<String\>        | Optional secondary delivery addresses.   |

**Rule:** a Buyer never manages information belonging to another Buyer, nor inventory
(spec Domain 2, RG03).

## Seller

Participant that registers and maintains products (spec Domain 3).

**Inherits:** `Person` (role `SELLER`)

| Attribute   | Type              | Notes                                             |
| ----------- | ----------------- | ------------------------------------------------ |
| status      | SellerStatus      | ACTIVE / SUSPENDED.                              |
| onboardedBy | User              | Administrator who registered the seller.        |
| warehouses  | List\<Warehouse\> | Seller-owned warehouses; the first is created at onboarding. |

**Rule:** sellers cannot self-register; they are onboarded by an `ADMIN` together with
their first warehouse (spec Domain 3, flow 6.1.1).

## Product (abstract)

Catalog item offered by a seller (spec Domain 5).

| Attribute   | Type                  | Notes                                          |
| ----------- | --------------------- | --------------------------------------------- |
| identifier  | String                | Unique catalog identifier.                     |
| name        | String                | Not empty.                                     |
| description | String                | Optional.                                      |
| type        | ProductType           | PHYSICAL or DIGITAL.                           |
| status      | ProductStatus         | PUBLISHED / SUSPENDED / DISCONTINUED.          |
| price       | Money                 | Non-negative.                                 |
| seller      | Seller                | Owner.                                        |
| category    | Category              | Classification.                              |
| variants    | List\<ProductVariant\>| Colour / size / model differences.            |

### PhysicalProduct

**Inherits:** `Product` (type `PHYSICAL`). Requires inventory and dispatch; an order
that contains at least one physical product waits for logistics after payment.

### DigitalProduct

**Inherits:** `Product` (type `DIGITAL`). No inventory; the order is delivered
immediately after successful payment.

## ProductVariant

| Attribute     | Type   | Notes                              |
| ------------- | ------ | ---------------------------------- |
| attributeName | String | e.g. `color`, `size`, `model`.     |
| value         | String | e.g. `red`, `M`, `2024`.           |

## Category

| Attribute | Type   |
| --------- | ------ |
| id        | Long   |
| name      | String |

## Warehouse

Physical storage location (spec Domain 4).

| Attribute  | Type          | Notes                                            |
| ---------- | ------------- | ----------------------------------------------- |
| identifier | String        | Unique.                                         |
| name       | String        | Not empty.                                      |
| type       | WarehouseType | MARKETPLACE or SELLER.                          |
| owner      | Seller?       | Present only when `type == SELLER`.             |
| location   | String        | Address / description.                          |

## InventoryItem

Distributed stock: always bound to one product **and** one warehouse (spec Domain 6).

| Attribute | Type                   | Notes                                        |
| --------- | ---------------------- | ------------------------------------------- |
| id        | Long                   |                                            |
| product   | Product                |                                            |
| warehouse | Warehouse              |                                            |
| stock     | StockQuantity          | Never negative (spec Domain 6, §11).        |
| condition | InventoryItemCondition | AVAILABLE / DAMAGED.                        |

**Rule:** stock marked `DAMAGED` cannot be reserved (spec §11).

## InventoryMovement

Immutable traceability record for every stock change (spec Domain 6).

| Attribute     | Type                  | Notes                                            |
| ------------- | --------------------- | ----------------------------------------------- |
| id            | Long                  |                                                |
| inventoryItem | InventoryItem         |                                                |
| type          | InventoryMovementType | ENTRY / RESERVATION / SALE_EXIT / ADJUSTMENT / RETURN. |
| quantity      | int                   | Positive.                                       |
| occurredOn    | LocalDateTime         |                                                |
| performedBy   | User                  |                                                |

## Cart

Provisional product selection before an order is committed (spec Domain 7, state 1).

| Attribute | Type             | Notes                          |
| --------- | ---------------- | ------------------------------ |
| id        | Long             |                                |
| buyer     | Buyer            |                                |
| items     | List\<CartItem\> |                                |
| status    | CartStatus       | ACTIVE / CONVERTED / ABANDONED |

### CartItem

| Attribute | Type            |
| --------- | --------------- |
| product   | Product         |
| variant   | ProductVariant? |
| quantity  | int             |

## Order

Formal commercial commitment; its lifecycle is the central process of the system
(spec Domain 7).

| Attribute | Type              | Notes                                            |
| --------- | ----------------- | ----------------------------------------------- |
| identifier| String            | Unique.                                         |
| buyer     | Buyer             |                                                |
| items     | List\<OrderItem\> |                                                |
| total     | Money             | Sum of item subtotals.                          |
| status    | OrderStatus       | CART → PENDING_PAYMENT → PAID → DISPATCHED → DELIVERED. |
| createdAt | LocalDateTime     |                                                |

**Rules:**

* A `DELIVERED` order is finalized and immutable (spec §11).
* Only valid status transitions are allowed; violations raise
  `InvalidStatusTransitionException`.

### OrderItem

| Attribute | Type            | Notes                                   |
| --------- | --------------- | --------------------------------------- |
| product   | Product         |                                        |
| variant   | ProductVariant? |                                        |
| quantity  | int             | Positive.                              |
| unitPrice | Money           | Price captured at checkout time.       |
| subtotal  | Money (derived) | `unitPrice × quantity`.                |

## Operation

A significant business action executed over an `AuditableEntity`.

| Attribute      | Type            |
| -------------- | --------------- |
| operationId    | Integer         |
| operationType  | OperationType   |
| executionDate  | LocalDateTime   |
| performedBy    | User            |
| affectedEntity | AuditableEntity |

An operation represents *the action that occurred*; it is distinct from the current
status of the affected entity.

## AuditLog

Immutable, append-only audit record. Persisted in MongoDB (the domain is unaware of
this).

| Attribute      | Type                  |
| -------------- | --------------------- |
| auditId        | String                |
| operationType  | OperationType         |
| operationDate  | LocalDateTime         |
| performedBy    | User                  |
| userRole       | UserRole              |
| affectedEntity | AuditableEntity       |
| severity       | AuditSeverity         |
| details        | Map\<String, Object\> |

**Rules:** immutable, append-only, never deleted; `userRole` is the role held when the
operation was performed.

## AuditableEntity (interface)

Marker implemented by entities that can be the subject of an `Operation` /
`AuditLog`. Exposes `auditableType()` and `auditableId()` for traceability.
Implemented by `User`, `Buyer`, `Seller`, `Warehouse`, `Product`, `InventoryItem`,
`Cart` and `Order`.

---

# Domain Lifecycle Relationship

The general shape of every significant business action:

```text
AuditableEntity
      │
      │ significant business action
      ▼
  Operation            operationType, performedBy, executionDate
      │
      │ audit registration
      ▼
  AuditLog             + userRole (at execution time), severity, details
```

For example, when an order is dispatched:

```text
Order
   │  status changes  PAID → DISPATCHED
   ▼
DISPATCHED
   │
   ├── Operation   operationType = ORDER_DISPATCH,  performedBy = LOGISTICS_OPERATOR
   │
   └── AuditLog    operationType = ORDER_DISPATCH,  affectedEntity = Order,
                   userRole = LOGISTICS_OPERATOR,   severity = INFO,
                   details = { order }
```

## Examples of Generated Operations

| Entity | Operations |
| ------ | ---------- |
| `User` | `USER_REGISTRATION`, `USER_STATUS_CHANGE` |
| `Buyer` | `USER_REGISTRATION` |
| `Seller` | `SELLER_ONBOARDING` |
| `Warehouse` | `WAREHOUSE_REGISTRATION` |
| `Product` | `PRODUCT_PUBLICATION`, `PRODUCT_STATUS_CHANGE` |
| `InventoryItem` | `INVENTORY_ENTRY`, `INVENTORY_RESERVATION`, `INVENTORY_RELEASE`, `INVENTORY_ADJUSTMENT`, `INVENTORY_RETURN` |
| `Order` | `CART_CHECKOUT`, `ORDER_PAYMENT`, `ORDER_DISPATCH`, `ORDER_DELIVERY` |

---

# Domain Design Rules

1. `User`, `Buyer` and `Seller` specialize `Person`; `role` is defined once in `Person`.
2. Each participant has exactly one role (spec RG02); a `User` never duplicates `role`.
3. Relationships use Domain Models, never primitive identifiers.
4. `identification` (document) and `email` are unique across the platform (spec §11).
5. Sellers are onboarded by an `ADMIN`, with their first warehouse, in a single flow.
6. Inventory is always `(product, warehouse)`; stock is never negative; `DAMAGED`
   stock cannot be reserved.
7. Every stock change produces an `InventoryMovement`.
8. The order lifecycle only advances through valid transitions; a `DELIVERED` order is
   immutable.
9. Digital-only orders move to `DELIVERED` on payment; orders with physical products
   stay `PAID` until dispatched by a `LOGISTICS_OPERATOR`.
10. Every significant business action produces an `Operation` and an `AuditLog` record.
