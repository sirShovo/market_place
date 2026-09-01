# Output Ports

## Introduction

Output Ports are the interfaces through which the domain talks to the outside world.
The domain **owns** them; adapters (persistence, payment, notification, security)
implement them in later phases.

Rules:

* Port methods work with **Domain Models**, not DTOs, persistence entities or bare
  primitive identifiers.
* Domain services never touch JPA, MongoDB, SQL, HTTP or Spring Data directly.
* A port does not necessarily map one-to-one to a database table.

Location: `application.domain.ports.out`.

---

# Repository Ports

## UserRepositoryPort

| Method | Purpose |
| ------ | ------- |
| `User save(User user)` | Persist a new user. |
| `Optional<User> findByUsername(User user)` | Look up by `username`. |
| `Optional<User> findByIdentification(User user)` | Look up by document. |
| `boolean existsByIdentification(User user)` | Uniqueness check (spec §11). |
| `boolean existsByEmail(User user)` | Uniqueness check (spec §11). |
| `void update(User user)` | Persist changes to an existing user. |

## BuyerRepositoryPort

`Buyer save(Buyer)`, `Optional<Buyer> findByIdentification(Buyer)`,
`boolean existsByIdentification(Buyer)`, `boolean existsByEmail(Buyer)`,
`void update(Buyer)`.

## SellerRepositoryPort

`Seller save(Seller)`, `Optional<Seller> findByIdentification(Seller)`,
`boolean existsByIdentification(Seller)`, `List<Seller> findAll()`,
`void update(Seller)`.

## WarehouseRepositoryPort

`Warehouse save(Warehouse)`, `Optional<Warehouse> findByIdentifier(Warehouse)`,
`List<Warehouse> findByOwner(Seller)`, `void update(Warehouse)`.

## CategoryRepositoryPort

`Category save(Category)`, `Optional<Category> findById(Category)`,
`List<Category> findAll()`.

## ProductRepositoryPort

`Product save(Product)`, `Optional<Product> findByIdentifier(Product)`,
`List<Product> findBySeller(Seller)`, `List<Product> findPublished()`,
`void update(Product)`.

## InventoryRepositoryPort

| Method | Purpose |
| ------ | ------- |
| `InventoryItem save(InventoryItem item)` | Persist an inventory item. |
| `Optional<InventoryItem> findByProductAndWarehouse(InventoryItem probe)` | Locate the `(product, warehouse)` item. |
| `List<InventoryItem> findByProduct(Product product)` | All stock of a product. |
| `void update(InventoryItem item)` | Persist stock / condition changes. |
| `InventoryMovement saveMovement(InventoryMovement movement)` | Append a traceability record. |
| `List<InventoryMovement> findMovements(InventoryItem item)` | History for an item. |

## CartRepositoryPort

`Cart save(Cart)`, `Optional<Cart> findActiveByBuyer(Buyer)`,
`Optional<Cart> findById(Cart)`, `void update(Cart)`.

## OrderRepositoryPort

`Order save(Order)`, `Optional<Order> findByIdentifier(Order)`,
`List<Order> findByBuyer(Buyer)`, `void update(Order)`.

## OperationRepositoryPort

`Operation save(Operation)`, `List<Operation> findByUser(User)`,
`List<Operation> findByEntity(AuditableEntity)`.

## AuditLogRepositoryPort

Expected MongoDB implementation; the domain stays unaware.

`AuditLog save(AuditLog)`, `List<AuditLog> findByUser(User)`,
`List<AuditLog> findByEntity(AuditableEntity)`.

---

# Service Ports

## PaymentGatewayPort

Abstracts the payment provider. The **simulation** (probabilistic approval / rejection
that lets a buyer retry) lives in the adapter, never in the domain.

```java
PaymentResult process(Order order);
```

## NotificationPort

```java
void send(Notification notification);
```

## PasswordServicePort

```java
String encrypt(String rawPassword);
boolean matches(String rawPassword, String encodedPassword);
```

## JwtServicePort

```java
String generateToken(User user);   // claims: username, role — never the password
```

## BusinessConfigurationPort

Externally configurable business parameters.

```java
int getUnpaidOrderExpirationMinutes();   // used by the Phase 7 scheduler
```

---

# Service → Port usage

| Subdomain | Ports typically used |
| --------- | -------------------- |
| user | `UserRepositoryPort`, `PasswordServicePort`, `OperationRepositoryPort`, `AuditLogRepositoryPort` |
| buyer | `BuyerRepositoryPort`, `OperationRepositoryPort`, `AuditLogRepositoryPort` |
| seller | `SellerRepositoryPort`, `WarehouseRepositoryPort`, `OperationRepositoryPort`, `AuditLogRepositoryPort` |
| warehouse | `WarehouseRepositoryPort`, `SellerRepositoryPort`, `OperationRepositoryPort`, `AuditLogRepositoryPort` |
| catalog | `ProductRepositoryPort`, `CategoryRepositoryPort`, `OperationRepositoryPort`, `AuditLogRepositoryPort` |
| inventory | `InventoryRepositoryPort`, `ProductRepositoryPort`, `WarehouseRepositoryPort`, `OperationRepositoryPort`, `AuditLogRepositoryPort` |
| cart | `CartRepositoryPort`, `ProductRepositoryPort` |
| order | `OrderRepositoryPort`, `CartRepositoryPort`, `InventoryRepositoryPort`, `PaymentGatewayPort`, `NotificationPort`, `OperationRepositoryPort`, `AuditLogRepositoryPort` |
| operation | `OperationRepositoryPort`, `AuditLogRepositoryPort` |

---

# Mandatory Rules

1. All output ports are interfaces owned by the domain.
2. Adapters implement them; adapters never contain business rules.
3. Ports use Domain Models, not DTOs / entities / bare identifiers.
4. Domain services depend only on ports, never on infrastructure.
5. The domain remains fully testable by substituting ports with test doubles.
