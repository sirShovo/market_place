# Persistence and Output Adapters

## Overview

Phase 5 implements the **Output Adapters** (`application.adapters.out` and `application.infrastructure.config`) that fulfill the contracts declared in `application.domain.ports.out`. In strict alignment with Hexagonal Architecture:

- **Domain Models** (`application.domain.models`) remain free of JPA (`jakarta.persistence.*`) and Spring Data MongoDB (`org.springframework.data.mongodb.*`) annotations.
- **Output Adapters** implement the domain's Output Ports, translate between Domain Models and persistence/external representations via dedicated **Mappers**, and delegate storage or transport operations to Spring Data repositories or infrastructure clients.
- **No business rules** live in the adapters; all state transitions, invariants, and validations remain inside the Domain layer.

```text
Domain Service
      │
      ▼
Output Port                (application.domain.ports.out.*)
      │
      ▼
Output Adapter             (application.adapters.out.**)
      │
      ├──► Persistence Mapper ──► JPA Entity / Mongo Document
      │
      ▼
Spring Data Repository / Infrastructure Client (MySQL, MongoDB, JavaMailSender, Random, Environment)
```

---

## 1. MySQL Relational Persistence (`application.adapters.out.persistence.mysql`)

All transactional operational state is stored in MySQL using Spring Data JPA. The package is divided into four sub-packages:

```text
application/adapters/out/persistence/mysql/
 ├── entities/        JPA @Entity and @Embeddable classes
 ├── repositories/    Spring Data JpaRepository interfaces
 ├── mappers/         Explicit Domain <-> JPA Entity mappers
 └── adapters/        @Component implementations of the 10 relational Output Ports
```

### 1.1 JPA Entities (`entities/`)

| JPA Entity | Table | Primary Key | Key Relationships & Columns |
|---|---|---|---|
| `UserJpaEntity` | `users` | `userId` (`Integer`, auto-increment) | Unique `username`, unique `identification`, unique `email`, `fullName`, `phoneNumber`, `address`, `role` (`code`), `password`, `status` (`code`) |
| `BuyerJpaEntity` | `buyers` | `identification` (`String`) | Unique `email`, `fullName`, `phoneNumber`, `address`, `role` (`code`), `commercialStatus` (`code`), `mainAddress`, `@ElementCollection` `additionalAddresses` (`buyer_additional_addresses`) |
| `SellerJpaEntity` | `sellers` | `identification` (`String`) | Unique `email`, `fullName`, `phoneNumber`, `address`, `role` (`code`), `status` (`code`), `@ManyToOne` `onboardedBy` (`UserJpaEntity`) |
| `WarehouseJpaEntity` | `warehouses` | `identifier` (`String`) | `name`, `type` (`code`), `@ManyToOne` `owner` (`SellerJpaEntity`, nullable for `MARKETPLACE`), `location` |
| `CategoryJpaEntity` | `categories` | `id` (`Long`, auto-increment) | `name` |
| `ProductJpaEntity` | `products` | `identifier` (`String`) | `name`, `description`, `productType` (`PHYSICAL` \| `DIGITAL`), `status` (`code`), `price` (`BigDecimal`), `@ManyToOne` `seller` (`SellerJpaEntity`), `@ManyToOne` `category` (`CategoryJpaEntity`), `@ElementCollection` `variants` (`ProductVariantEmbeddable` in `product_variants`) |
| `ProductVariantEmbeddable` | `product_variants` | — (`@Embeddable`) | `attributeName`, `attributeValue` |
| `InventoryItemJpaEntity` | `inventory_items` | `id` (`Long`, auto-increment) | `@ManyToOne` `product` (`ProductJpaEntity`), `@ManyToOne` `warehouse` (`WarehouseJpaEntity`), `stock` (`int`), `itemCondition` (`code`) |
| `InventoryMovementJpaEntity` | `inventory_movements` | `id` (`Long`, auto-increment) | `@ManyToOne` `inventoryItem` (`InventoryItemJpaEntity`), `type` (`code`), `quantity` (`int`), `occurredOn` (`LocalDateTime`), `@ManyToOne` `performedBy` (`UserJpaEntity`, nullable) |
| `CartJpaEntity` | `carts` | `id` (`Long`, auto-increment) | `@ManyToOne` `buyer` (`BuyerJpaEntity`), `status` (`code`), `@OneToMany(cascade = ALL, orphanRemoval = true)` `items` (`CartItemJpaEntity`) |
| `CartItemJpaEntity` | `cart_items` | `id` (`Long`, auto-increment) | `@ManyToOne` `cart` (`CartJpaEntity`), `@ManyToOne` `product` (`ProductJpaEntity`), `variantAttributeName`, `variantValue`, `quantity` (`int`) |
| `OrderJpaEntity` | `orders` | `identifier` (`String`) | `@ManyToOne` `buyer` (`BuyerJpaEntity`), `total` (`BigDecimal`), `status` (`code`), `createdAt` (`LocalDateTime`), `@OneToMany(cascade = ALL, orphanRemoval = true)` `items` (`OrderItemJpaEntity`) |
| `OrderItemJpaEntity` | `order_items` | `id` (`Long`, auto-increment) | `@ManyToOne` `order` (`OrderJpaEntity`), `@ManyToOne` `product` (`ProductJpaEntity`), `variantAttributeName`, `variantValue`, `quantity` (`int`), `unitPrice` (`BigDecimal`), `@ManyToOne` `warehouse` (`WarehouseJpaEntity`, nullable for digital lines) |
| `OperationJpaEntity` | `operations` | `operationId` (`Integer`, auto-increment) | `operationType` (`code`), `executionDate` (`LocalDateTime`), `@ManyToOne` `performedBy` (`UserJpaEntity`, nullable), `affectedEntityType` (`String`), `affectedEntityId` (`String`) |

### 1.2 Spring Data JPA Repositories (`repositories/`)

Each entity is backed by a dedicated `JpaRepository` interface exposing derived query methods that support the corresponding Output Port:

- `UserJpaRepository`: `findByUsername(String)`, `findByIdentification(String)`, `existsByIdentification(String)`, `existsByEmail(String)`
- `BuyerJpaRepository`: `existsByIdentification(String)`, `existsByEmail(String)`
- `SellerJpaRepository`: `existsByIdentification(String)`, `existsByEmail(String)`
- `WarehouseJpaRepository`: `findByOwner_Identification(String)`
- `CategoryJpaRepository`: standard `JpaRepository<CategoryJpaEntity, Long>`
- `ProductJpaRepository`: `findBySeller_Identification(String)`, `findByStatus(String)`
- `InventoryItemJpaRepository`: `findByProduct_IdentifierAndWarehouse_Identifier(String, String)`, `findByProduct_Identifier(String)`
- `InventoryMovementJpaRepository`: `findByInventoryItem_Id(Long)`
- `CartJpaRepository`: `findByBuyer_IdentificationAndStatus(String, String)`
- `OrderJpaRepository`: `findByBuyer_Identification(String)`
- `OperationJpaRepository`: `findByPerformedBy_Username(String)`, `findByAffectedEntityTypeAndAffectedEntityId(String, String)`

### 1.3 Persistence Mappers (`mappers/`)

Every aggregate has an explicit `@Component` mapper in `application.adapters.out.persistence.mysql.mappers`:

- `UserMapper`, `BuyerMapper`, `SellerMapper`, `WarehouseMapper`, `CategoryMapper`, `ProductMapper`, `InventoryMapper`, `CartMapper`, `OrderMapper`, `OperationMapper`.
- **Value Objects**: `DocumentId`, `Email`, `Money`, and `StockQuantity` are unwrapped into primitive/standard types (`String`, `BigDecimal`, `int`) on write and re-instantiated on read.
- **Controlled Catalogs (`DomainCatalog`)**: Persisted by their canonical `code` (`String`) and resolved back into singletons via `fromCode(String)`.
- **Product Polymorphism**: `ProductMapper` inspects `ProductJpaEntity.getProductType()` (`PHYSICAL` vs. `DIGITAL`) and instantiates `PhysicalProduct` or `DigitalProduct` accordingly.
- **Order Rehydration**: `OrderMapper` populates `OrderItem`s, `total`, `status`, and `createdAt` directly on the `Order` model so that finalized (`DELIVERED`) orders can be read from the database without triggering `FinalizedOrderModificationException`.

### 1.4 MySQL Repository Adapters (`adapters/`)

| Output Port | Adapter Implementation |
|---|---|
| `UserRepositoryPort` | `UserMySqlAdapter` |
| `BuyerRepositoryPort` | `BuyerMySqlAdapter` |
| `SellerRepositoryPort` | `SellerMySqlAdapter` |
| `WarehouseRepositoryPort` | `WarehouseMySqlAdapter` |
| `CategoryRepositoryPort` | `CategoryMySqlAdapter` |
| `ProductRepositoryPort` | `ProductMySqlAdapter` |
| `InventoryRepositoryPort` | `InventoryMySqlAdapter` |
| `CartRepositoryPort` | `CartMySqlAdapter` |
| `OrderRepositoryPort` | `OrderMySqlAdapter` |
| `OperationRepositoryPort` | `OperationMySqlAdapter` |

---

## 2. MongoDB Document Persistence (`application.adapters.out.persistence.mongodb`)

Immutable, append-only `AuditLog` records are stored in MongoDB (`audit_logs` collection) so that high-volume audit writes and flexible `details` maps (`Map<String, Object>`) are isolated from the relational schema.

```text
application/adapters/out/persistence/mongodb/
 ├── documents/       AuditLogDocument (@Document(collection = "audit_logs"))
 ├── repositories/    AuditLogMongoRepository (MongoRepository<AuditLogDocument, String>)
 ├── mappers/         AuditLogDocumentMapper
 └── adapters/        AuditLogMongoAdapter (implements AuditLogRepositoryPort)
```

- `AuditLogDocument` captures `auditId` (`@Id`), `operationType`, `operationDate`, `performedByUserId`, `performedByUsername`, `userRole`, `affectedEntityType`, `affectedEntityId`, `severity`, and `details` (`Map<String, Object>`).
- `AuditLogMongoAdapter` implements `AuditLogRepositoryPort` (`save`, `findByUser`, `findByEntity`).

---

## 3. External Service & Configuration Output Adapters

### 3.1 `SimulatedPaymentAdapter` (`application.adapters.out.payment`)

Implements `PaymentGatewayPort`:

- Simulates an external payment processor with a configurable approval probability (`app.payment.approval-probability`, default `0.7`).
- Returns `PaymentResult.APPROVED` when the random draw is below the configured threshold (or `1.0`), and `PaymentResult.REJECTED` otherwise, enabling buyers to retry payment on orders that remain in `PENDING_PAYMENT`.
- Exposes a deterministic constructor taking a `DoubleSupplier` (or probability + `Random`) for unit testing.

### 3.2 `EmailNotificationAdapter` (`application.adapters.out.notification`)

Implements `NotificationPort`:

- Builds a `SimpleMailMessage` from the domain `Notification` (`recipient`, `subject`, `body`) and dispatches it via Spring's `JavaMailSender`.
- Degrades gracefully (logging a warning instead of aborting the business transaction) if the SMTP server is unreachable or unconfigured in local/test environments.

### 3.3 `PropertiesConfigurationAdapter` (`application.infrastructure.config`)

Implements `BusinessConfigurationPort`:

- Reads `app.order.unpaid-expiration-minutes` (default `30`) from Spring configuration and provides it to the domain and scheduler components.
