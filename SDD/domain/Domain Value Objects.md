# Domain Value Objects

## Introduction

Value Objects are immutable concepts compared by value. They keep primitive values and
scattered string literals out of the domain.

NexusMarket uses three kinds:

1. **Catalog value objects** — controlled business catalogs that extend the abstract
   `DomainCatalog` (`code`, `name`, `description`; equality by `code`).
2. **Primitive wrappers** — small records that wrap and validate a single primitive.
3. **Plain enums** — fixed technical values without business metadata.

---

# DomainCatalog (abstract)

```java
public abstract class DomainCatalog {
    private final String code;         // unique business identifier, used for equality
    private final String name;         // human-readable name
    private final String description;  // business definition
}
```

* Immutable.
* Equality and hash code are based solely on `code`.
* Concrete catalogs expose their allowed values as `public static final` instances.

---

# Catalog Value Objects

## UserRole

Responsibilities and permissions of a participant. Exactly one per participant
(spec §5, RG02).

| Code               | Name               |
| ------------------ | ------------------ |
| BUYER              | Buyer              |
| SELLER             | Seller             |
| LOGISTICS_OPERATOR | Logistics Operator |
| ADMIN              | Administrator      |
| SUPERVISOR         | Supervisor         |

## UserStatus

System-access status of a `User` (spec Domain 1).

| Code     | Name     |
| -------- | -------- |
| ACTIVE   | Active   |
| BLOCKED  | Blocked  |
| INACTIVE | Inactive |

## BuyerCommercialStatus

Ability of a `Buyer` to operate commercially (spec Domain 2).

| Code      | Name      |
| --------- | --------- |
| ACTIVE    | Active    |
| SUSPENDED | Suspended |

## SellerStatus

Operational status of a `Seller` (spec Domain 3).

| Code      | Name      |
| --------- | --------- |
| ACTIVE    | Active    |
| SUSPENDED | Suspended |

## WarehouseType

Classification of storage locations (spec Domain 4).

| Code        | Name                 | Notes                          |
| ----------- | -------------------- | ------------------------------ |
| MARKETPLACE | Marketplace warehouse | Operated by the marketplace.  |
| SELLER      | Seller warehouse      | Owned by a specific seller.   |

## ProductType

Fulfillment nature of a catalog item (spec Domain 5).

| Code     | Name             | Notes                                    |
| -------- | ---------------- | ---------------------------------------- |
| PHYSICAL | Physical product | Requires inventory and dispatch.         |
| DIGITAL  | Digital product  | Delivered immediately after payment.     |

## ProductStatus

Catalog visibility of a product (spec Domain 5).

| Code         | Name         |
| ------------ | ------------ |
| PUBLISHED    | Published    |
| SUSPENDED    | Suspended    |
| DISCONTINUED | Discontinued |

## OrderStatus

Order lifecycle (spec Domain 7). `DELIVERED` is terminal and immutable.

| Code            | Name            | Notes                                    |
| --------------- | --------------- | ---------------------------------------- |
| CART            | Cart            | Provisional selection.                   |
| PENDING_PAYMENT | Pending payment | Awaiting financial confirmation.         |
| PAID            | Paid            | Fulfillment starts.                      |
| DISPATCHED      | Dispatched      | Physically left the warehouse.           |
| DELIVERED       | Delivered       | Finalized; immutable (spec §11).         |

Allowed transitions:

```text
CART → PENDING_PAYMENT → PAID → DISPATCHED → DELIVERED
PENDING_PAYMENT → PAID → DELIVERED        (digital-only orders)
```

## CartStatus

| Code      | Name      | Notes                                  |
| --------- | --------- | ------------------------------------- |
| ACTIVE    | Active    | Being edited by the buyer.            |
| CONVERTED | Converted | Turned into an order at checkout.     |
| ABANDONED | Abandoned | Discarded.                            |

## InventoryMovementType

Type of stock movement (spec Domain 6).

| Code        | Name        |
| ----------- | ----------- |
| ENTRY       | Entry       |
| RESERVATION | Reservation |
| SALE_EXIT   | Sale exit   |
| ADJUSTMENT  | Adjustment  |
| RETURN      | Return      |

## InventoryItemCondition

| Code      | Name      | Notes                                      |
| --------- | --------- | ----------------------------------------- |
| AVAILABLE | Available | Can be reserved and sold.                 |
| DAMAGED   | Damaged   | Cannot be reserved (spec §11).            |

## OperationType

Significant business operations recorded in the audit trail.

| Code                   | Name                    |
| ---------------------- | ----------------------- |
| USER_REGISTRATION      | User registration        |
| USER_STATUS_CHANGE     | User status change       |
| SELLER_ONBOARDING      | Seller onboarding        |
| WAREHOUSE_REGISTRATION | Warehouse registration   |
| PRODUCT_PUBLICATION    | Product publication      |
| PRODUCT_STATUS_CHANGE  | Product status change    |
| INVENTORY_ENTRY        | Inventory entry          |
| INVENTORY_RESERVATION  | Inventory reservation    |
| INVENTORY_RELEASE      | Inventory release        |
| INVENTORY_ADJUSTMENT   | Inventory adjustment     |
| INVENTORY_RETURN       | Inventory return         |
| CART_CHECKOUT          | Cart checkout            |
| ORDER_PAYMENT          | Order payment            |
| ORDER_DISPATCH         | Order dispatch           |
| ORDER_DELIVERY         | Order delivery           |

---

# Primitive Wrappers

Implemented as Java `record`s (value equality for free); each validates its input on
construction and throws `DomainException` (or a subclass) on violation.

## Money

| Field  | Type       |
| ------ | ---------- |
| amount | BigDecimal |

* Rejects `null` and negative amounts.
* `zero()`, `of(BigDecimal)`; `add`, `subtract`, `multiply(int)`.
* Single implicit currency (the specification does not require multi-currency).

## Email

| Field | Type   |
| ----- | ------ |
| value | String |

* Rejects `null` and values not matching a basic e-mail pattern.

## DocumentId

| Field | Type   |
| ----- | ------ |
| value | String |

* Rejects `null` / blank.

## StockQuantity

| Field | Type |
| ----- | ---- |
| value | int  |

* Rejects negative values (`NegativeStockException`).
* `of(int)`; `add(int)`, `subtract(int)` (subtraction below zero throws
  `NegativeStockException`).

---

# Plain Enums

| Enum                | Values                              | Purpose                              |
| ------------------- | ----------------------------------- | ------------------------------------ |
| PaymentResult       | APPROVED, REJECTED                  | Outcome returned by `PaymentGatewayPort`. |
| NotificationChannel | EMAIL, SMS, PUSH                    | Delivery channel for a `Notification`. |
| AuditSeverity       | INFO, WARNING, ERROR, CRITICAL      | Severity level of an audit record.   |

---

# Rules

1. Value Objects are immutable and compared by value.
2. Controlled business concepts use catalog VOs, never bare strings.
3. Plain enums are reserved for fixed technical values without business metadata.
4. Primitive wrappers validate their invariants at construction time.
