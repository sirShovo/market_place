# Output Ports

## Introduction

Output Ports define the contracts through which the **Domain** communicates with
external resources. The Domain **owns** these interfaces; their implementations belong
to the Adapter layer (persistence, payment, notification, security) and are delivered
in phases 5–6.

Domain services must never depend directly on:

- MySQL / MongoDB / JPA / Spring Data
- SQL
- HTTP / REST / external API clients
- a payment SDK, a JWT library or a password-hashing library

When a domain service needs information or a capability outside the Domain, it uses
the corresponding Output Port.

Location: `application.domain.ports.out`.

---

## Architectural Rule

```text
Domain Service
      │
      ▼
Output Port                (interface, owned by the domain)
      │
      ▼
Output Adapter             (implements the port; no business rules)
      │
      ▼
External Resource          (MySQL, MongoDB, SMTP, payment provider, ...)
```

For persistence:

```text
OrderService → OrderRepositoryPort → OrderMySqlAdapter → MySQL
```

For the audit trail:

```text
RegisterAuditLogService → AuditLogRepositoryPort → AuditLogMongoAdapter → MongoDB
```

---

## General Parameter Rule

Output Port methods work with **Domain Models**. They must not receive DTOs,
persistence entities, or bare primitive identifiers when a Domain Model already
represents the concept.

Incorrect:

```java
Optional<Order> findById(String orderId);
void updateStock(Long itemId, int newValue);
```

Correct:

```java
Optional<Order> findByIdentifier(Order order);   // 'order' carries the identifier
void update(InventoryItem item);
```

Where a lookup needs a `(product, warehouse)` pair, an `InventoryItem` *probe* — a
partially populated model — is passed rather than two `String` ids.

---

# Repository Ports

## 1. UserRepositoryPort

**Responsibility:** persistence and queries for `User`.

```java
public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findByUsername(User user);
    Optional<User> findByIdentification(User user);
    boolean existsByIdentification(User user);   // platform-wide uniqueness (spec §11)
    boolean existsByEmail(User user);            // platform-wide uniqueness (spec §11)
    void update(User user);
}
```

**Main consumers:** `RegisterUserService`, `ChangeUserStatusService`,
`ConsultUserService`.

## 2. BuyerRepositoryPort

**Responsibility:** persistence and queries for `Buyer`.

```java
public interface BuyerRepositoryPort {
    Buyer save(Buyer buyer);
    Optional<Buyer> findByIdentification(Buyer buyer);
    boolean existsByIdentification(Buyer buyer);
    boolean existsByEmail(Buyer buyer);
    void update(Buyer buyer);
}
```

**Main consumers:** `RegisterBuyerService`, `UpdateBuyerService`,
`ConsultBuyerService`.

## 3. SellerRepositoryPort

**Responsibility:** persistence and queries for `Seller`.

```java
public interface SellerRepositoryPort {
    Seller save(Seller seller);
    Optional<Seller> findByIdentification(Seller seller);
    boolean existsByIdentification(Seller seller);
    List<Seller> findAll();
    void update(Seller seller);
}
```

**Main consumers:** `OnboardSellerService`, `ConsultSellerService`.

## 4. WarehouseRepositoryPort

**Responsibility:** persistence and queries for `Warehouse`.

```java
public interface WarehouseRepositoryPort {
    Warehouse save(Warehouse warehouse);
    Optional<Warehouse> findByIdentifier(Warehouse warehouse);
    List<Warehouse> findByOwner(Seller owner);
    void update(Warehouse warehouse);
}
```

**Main consumers:** `OnboardSellerService`, `RegisterWarehouseService`,
`ConsultWarehouseService`, inventory services (to resolve the target warehouse).

## 5. CategoryRepositoryPort

**Responsibility:** persistence and queries for `Category`.

```java
public interface CategoryRepositoryPort {
    Category save(Category category);
    Optional<Category> findById(Category category);
    List<Category> findAll();
}
```

**Main consumers:** catalog services.

## 6. ProductRepositoryPort

**Responsibility:** persistence and queries for `Product` (both `PhysicalProduct` and
`DigitalProduct`).

```java
public interface ProductRepositoryPort {
    Product save(Product product);
    Optional<Product> findByIdentifier(Product product);
    List<Product> findBySeller(Seller seller);
    List<Product> findPublished();
    void update(Product product);
}
```

**Main consumers:** `PublishProductService`, `UpdateProductService`,
`ChangeProductStatusService`, `ConsultCatalogService`, cart services.

## 7. InventoryRepositoryPort

**Responsibility:** persistence for `InventoryItem` plus its append-only
`InventoryMovement` trail.

```java
public interface InventoryRepositoryPort {
    InventoryItem save(InventoryItem item);
    Optional<InventoryItem> findByProductAndWarehouse(InventoryItem probe);
    List<InventoryItem> findByProduct(Product product);
    void update(InventoryItem item);
    InventoryMovement saveMovement(InventoryMovement movement);
    List<InventoryMovement> findMovements(InventoryItem item);
}
```

**Main consumers:** all inventory services.

> A port does not map one-to-one to a table: the adapter may persist `InventoryItem`
> and `InventoryMovement` in separate tables, and `Product` across
> `product` / `physical_product` / `digital_product`. The domain only knows the port.

## 8. CartRepositoryPort

**Responsibility:** persistence and queries for `Cart`.

```java
public interface CartRepositoryPort {
    Cart save(Cart cart);
    Optional<Cart> findActiveByBuyer(Buyer buyer);
    Optional<Cart> findById(Cart cart);
    void update(Cart cart);
}
```

**Main consumers:** cart services, `CheckoutCartService`.

## 9. OrderRepositoryPort

**Responsibility:** persistence and queries for `Order`.

```java
public interface OrderRepositoryPort {
    Order save(Order order);
    Optional<Order> findByIdentifier(Order order);
    List<Order> findByBuyer(Buyer buyer);
    void update(Order order);
}
```

**Main consumers:** all order services.

## 10. OperationRepositoryPort

**Responsibility:** persistence for business `Operation` records.

```java
public interface OperationRepositoryPort {
    Operation save(Operation operation);
    List<Operation> findByUser(User user);
    List<Operation> findByEntity(AuditableEntity entity);
}
```

**Main consumers:** `RegisterOperationService` (and, transitively, every command
service via `RegisterOperationAndAuditService`).

## 11. AuditLogRepositoryPort

**Responsibility:** persistence for immutable `AuditLog` records. Expected MongoDB
implementation; the domain stays unaware.

```java
public interface AuditLogRepositoryPort {
    AuditLog save(AuditLog auditLog);
    List<AuditLog> findByUser(User user);
    List<AuditLog> findByEntity(AuditableEntity entity);
}
```

**Main consumers:** `RegisterAuditLogService`, `ConsultAuditLogService`.

---

# Service Ports

## 12. PaymentGatewayPort

**Responsibility:** abstracts the payment provider. The **simulation** — probabilistic
approval / rejection that lets a buyer retry — lives in the adapter, never in the
domain.

```java
public interface PaymentGatewayPort {
    PaymentResult process(Order order);   // APPROVED | REJECTED
}
```

**Main consumer:** `ProcessOrderPaymentService`.

## 13. NotificationPort

**Responsibility:** abstracts communication with external notification systems. The
domain builds a `Notification` (channel, recipient, subject, body); the adapter knows
how to deliver it.

```java
public interface NotificationPort {
    void send(Notification notification);
}
```

**Main consumer:** `ProcessOrderPaymentService` (payment confirmation). Future
consumers: order dispatch / delivery notifications.

## 14. PasswordServicePort

**Responsibility:** abstracts password hashing and verification. The domain must not
depend on BCrypt/Argon2 or Spring Security.

```java
public interface PasswordServicePort {
    String encrypt(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
```

**Main consumers:** `RegisterUserService` (encrypt); the Phase 6 login service
(matches).

## 15. JwtServicePort

**Responsibility:** abstracts JWT generation. The token may carry claims such as
`username` and `role`, **never** the password.

```java
public interface JwtServicePort {
    String generateToken(User user);
}
```

**Main consumer:** the Phase 6 authentication service.

## 16. BusinessConfigurationPort

**Responsibility:** externally configurable business parameters that must not be
hardcoded in the domain.

```java
public interface BusinessConfigurationPort {
    int getUnpaidOrderExpirationMinutes();   // used by the Phase 7 scheduler
}
```

**Main consumer:** the Phase 7 order-expiration scheduler (and any service that
releases reservations for expired orders).

---

# Port Organization

```text
application/domain/ports/out/
 ├── UserRepositoryPort
 ├── BuyerRepositoryPort
 ├── SellerRepositoryPort
 ├── WarehouseRepositoryPort
 ├── CategoryRepositoryPort
 ├── ProductRepositoryPort
 ├── InventoryRepositoryPort
 ├── CartRepositoryPort
 ├── OrderRepositoryPort
 ├── OperationRepositoryPort
 ├── AuditLogRepositoryPort
 ├── PaymentGatewayPort
 ├── NotificationPort
 ├── PasswordServicePort
 ├── JwtServicePort
 └── BusinessConfigurationPort
```

---

# Mapping to Adapters (phases 5–6)

```text
UserRepositoryPort        ← UserMySqlAdapter
BuyerRepositoryPort       ← BuyerMySqlAdapter
SellerRepositoryPort      ← SellerMySqlAdapter
WarehouseRepositoryPort   ← WarehouseMySqlAdapter
CategoryRepositoryPort    ← CategoryMySqlAdapter
ProductRepositoryPort     ← ProductMySqlAdapter
InventoryRepositoryPort   ← InventoryMySqlAdapter
CartRepositoryPort        ← CartMySqlAdapter
OrderRepositoryPort       ← OrderMySqlAdapter
OperationRepositoryPort   ← OperationMySqlAdapter
AuditLogRepositoryPort    ← AuditLogMongoAdapter
PaymentGatewayPort        ← SimulatedPaymentAdapter     (probabilistic approve/reject)
NotificationPort          ← EmailNotificationAdapter     (JavaMailSender)
PasswordServicePort       ← BCryptPasswordAdapter
JwtServicePort            ← JjwtAdapter
BusinessConfigurationPort ← PropertiesConfigurationAdapter
```

---

# Final Architectural Rules

1. All Output Ports are interfaces owned by the Domain.
2. Adapters implement Output Ports and contain no business rules.
3. Ports use Domain Models, never DTOs, persistence entities or bare identifiers.
4. Domain services depend only on ports, never on infrastructure.
5. A port does not necessarily correspond to a single database table.
6. The audit trail port is expected to be backed by MongoDB; the domain never knows.
7. The payment simulation is adapter behavior, not domain behavior.
8. The Domain remains fully testable by substituting every port with a test double.
