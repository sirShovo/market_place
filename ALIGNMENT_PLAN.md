# Plan de Alineación — `market_place` con el estándar de `bank` (+ SPECS.md)

## Contexto

El proyecto `market_place` (NexusMarket) tiene sus fases 1–4 "implementadas", pero
**no sigue las convenciones del proyecto de referencia del profesor** (`bank`, en
`construccion_de_software_2_2026_2`). Antes de continuar con la fase 5 (persistencia),
hay que corregir hasta la fase 4 para que el repo cumpla los mismos criterios que
`bank`, contemplando la especificación funcional `specs.md` (NexusMarket), y dejar
el SDD tan completo como el del profesor.

Se mantiene el trabajo **por fases**. El orden dentro de cada fase es: **primero el
SDD objetivo, luego el código que lo implementa** (igual que construyó el profe:
commits de documentación antes que los de código).

---

## Decisiones tomadas

| Tema | Decisión |
|---|---|
| Value Objects de catálogo | **Híbrido como el profe**: `DomainCatalog` (code/name/description, igualdad por `code`) para conceptos de negocio con significado y presencia en reportes; `enum` plano solo para binarios/técnicos. |
| Simulación de pago | **Puerto `PaymentGatewayPort` en el dominio** + la simulación con fallo/reintento vive en un **adapter de salida** (fase 5). Se elimina `new Random()` del dominio. |
| Auditoría | **Adoptar ya** el patrón `Operation` + `AuditLog` transversal del profe (modelos + puertos + `RegisterOperationAndAuditService` en fases 2–4; persistencia Mongo en fase 5). |
| Secuencia | **SDD objetivo primero, luego código.** |
| Cobertura de dominio en este pase | **Flujo central + Operation/Audit + autorización**: usuarios (5 roles), compradores, vendedores, bodegas, catálogo (variantes + estados), inventario (movimientos + condición `DAMAGED`), carrito y pedidos. Facturación, envíos, devoluciones, reembolsos y reportes → fase posterior. |

---

## Convenciones objetivo (tomadas de `bank`)

1. **Paquete raíz `application`**; dominio en `application.domain.*`
   (`models/`, `valueobjects/`, `enums/`, `services/`, `exceptions/`, `ports/{in,out}/`).
2. **Lombok** pervasivo: `@Getter @Setter @NoArgsConstructor` en modelos,
   `@RequiredArgsConstructor` en servicios, `@EqualsAndHashCode(onlyExplicitlyIncluded=true)` en `DomainCatalog`.
3. **Modelos con herencia real y relaciones por referencia a objetos**, nunca por IDs
   primitivos (`Order.buyer : Buyer`, no `Order.buyerId : Long`).
4. **Value Objects de catálogo** vía `DomainCatalog` + instancias `public static final`.
5. **Un servicio de dominio por caso de uso**, en carpetas por subdominio, anotado
   `@Service`, método `execute(...)`, que compone otros servicios (autorización, auditoría).
6. **Puertos de salida en el dominio**, reciben **modelos de dominio** como parámetros.
7. **Toda acción significativa** genera `Operation` + `AuditLog`.
8. **SDD extenso**, con un archivo por subdominio de servicios.
9. `@Service` + inyección por constructor ⇒ Spring cablea solo (sin clase de `@Bean`).

---

## PARTE A — SDD objetivo (se hace primero, por fase)

Reestructurar `SDD/` para reflejar la del profe. Unificar el nombre del producto en
**NexusMarket** (título de `specs.md` y de los SDD actuales; actualizar `README.md`).

```
SDD/
├── Software Architecture/
│   └── Software Architecture.md          (desde 01_ARCHITECTURE_OVERVIEW.md, ampliado: capas, reglas de dependencia, constraints)
├── domain/
│   ├── Domain Model.md                   (desde 02_DOMAIN_LAYER.md: jerarquía de clases, atributos, relaciones, reglas)
│   ├── Domain Value Objects.md           (DomainCatalog + cada catálogo con su tabla de valores permitidos; enums planos)
│   ├── Domain Services.md                (catálogo de servicios de alto nivel por subdominio)
│   ├── Output Ports.md                   (desde 04_APPLICATION_LAYER.md: cada puerto out, responsabilidad, métodos, consumidores)
│   ├── Input Ports.md                    (interfaces de caso de uso)
│   └── services/
│       ├── user-services.md
│       ├── buyer-services.md
│       ├── seller-services.md
│       ├── warehouse-services.md
│       ├── catalog-services.md
│       ├── inventory-services.md
│       ├── cart-services.md
│       ├── order-services.md
│       ├── authorization-services.md
│       └── operation-audit-services.md
```

Archivos actuales `SDD/0X_*.md` y `IMPLEMENTATION_PLAN.md`: migrar contenido y luego
retirar los `0X_` (o dejarlos como redirección). Cada servicio del código debe tener
su sección en `SDD/domain/services/*` (entrada, validaciones de dominio, autorización,
puertos usados, operación/auditoría generada, excepciones).

---

## PARTE B — Código (por subdominio, tras su SDD)

### B0 · Base y estructura → *fase 1*

- **`pom.xml`**: quitar los atributos `xmlns=""` malformados; alinear el parent de
  Spring Boot con `bank` (`4.1.0`); conservar los starters tal como los tiene `bank`
  (`data-jpa`, `mongodb`, `security`, `webmvc` + `*-test`); Lombok ya está como
  annotation processor. Rellenar `<name>`/`<description>` = `bank`-style.
- **Paquetes**: mover todo a raíz `application`; crear `application.domain.*` y
  `application.infrastructure.config`.
- **`config/` → `application.infrastructure.config`**: `GlobalExceptionHandler`,
  `JacksonConfig`. `ApiResponse` se mueve ahí también, marcado como placeholder de
  fase 6 (REST). `bank` aún no los tiene; se conservan porque no estorban.
- **`MarketPlaceApplication`** → `NexusMarketApplication`, paquete `application`,
  quitar el `@ComponentScan` amplio (con la raíz `application` es innecesario).
- Activar el uso real de **Lombok**.

### B1 · Value Objects → *fase 2*

- **`DomainCatalog`** abstracto (copiar patrón de `bank`).
- **Catálogos** (`final class ... extends DomainCatalog` con `static final`):
  | Catálogo | Valores (de `specs.md`) |
  |---|---|
  | `UserRole` | BUYER, SELLER, LOGISTICS_OPERATOR, ADMIN, SUPERVISOR (§5, RG02: uno por usuario) |
  | `UserStatus` | ACTIVE, BLOCKED, INACTIVE (Dominio 1) |
  | `BuyerCommercialStatus` | ACTIVE, SUSPENDED (Dominio 2: "estado comercial") |
  | `SellerStatus` | ACTIVE, SUSPENDED (Dominio 3) |
  | `WarehouseType` | MARKETPLACE, SELLER (Dominio 4) |
  | `ProductType` | PHYSICAL, DIGITAL (Dominio 5) |
  | `ProductStatus` | PUBLISHED, SUSPENDED, DISCONTINUED (Dominio 5) |
  | `OrderStatus` | CART, PENDING_PAYMENT, PAID, DISPATCHED, DELIVERED (Dominio 7; DELIVERED = finalizado, terminal) |
  | `CartStatus` | ACTIVE, CONVERTED, ABANDONED |
  | `InventoryMovementType` | ENTRY, RESERVATION, SALE_EXIT, ADJUSTMENT, RETURN (Dominio 6) |
  | `InventoryItemCondition` | AVAILABLE, DAMAGED (§11: no se reserva lo `DAMAGED`) |
  | `OperationType` | catálogo de operaciones significativas (USER_REGISTRATION, SELLER_ONBOARDING, WAREHOUSE_REGISTRATION, PRODUCT_PUBLICATION, PRODUCT_STATUS_CHANGE, INVENTORY_ENTRY, INVENTORY_RESERVATION, INVENTORY_ADJUSTMENT, INVENTORY_RETURN, CART_CHECKOUT, ORDER_PAYMENT, ORDER_DISPATCH, ORDER_DELIVERY) |
- **Enums planos**: `PaymentResult` (APPROVED, REJECTED), `NotificationChannel`
  (EMAIL, SMS, PUSH), `AuditSeverity` (INFO, WARNING, ERROR, CRITICAL).
- **VOs de primitivo** que `market_place` ya tiene y se **conservan** (más estrictos
  que `bank`, pero avalados por la doc del profe): `Money`, `Email`, `DocumentId`,
  `StockQuantity`. Añadir **igualdad por valor** (`@EqualsAndHashCode` / `record`).
- **Retirar** los `enum` actuales sustituidos por catálogos.

### B2 · Modelos → *fase 2*

```
Person (abstract): identification: DocumentId, name, email: Email, phoneNumber, address, role: UserRole
├── User   : userId, username, password, status: UserStatus
├── Buyer  : commercialStatus: BuyerCommercialStatus, mainAddress, additionalAddresses: List<String>
└── Seller : status: SellerStatus, onboardedBy: User, warehouses: List<Warehouse>
      (LOGISTICS_OPERATOR / ADMIN / SUPERVISOR = solo User con su role)

Product (abstract): identifier, name, description, type: ProductType, status: ProductStatus,
                    price: Money, seller: Seller, category: Category, variants: List<ProductVariant>
├── PhysicalProduct
└── DigitalProduct

ProductVariant : attributeName (color/talla/modelo), value
Category

Warehouse      : identifier, name, type: WarehouseType, owner: Seller?, location
InventoryItem  : product: Product, warehouse: Warehouse, stock: StockQuantity, condition: InventoryItemCondition
InventoryMovement : inventoryItem: InventoryItem, type: InventoryMovementType, quantity, occurredOn, performedBy: User

Cart      : buyer: Buyer, items: List<CartItem>, status: CartStatus
CartItem  : product: Product, variant: ProductVariant?, quantity
Order     : identifier, buyer: Buyer, items: List<OrderItem>, total: Money, status: OrderStatus, createdAt
OrderItem : product: Product, variant: ProductVariant?, quantity, unitPrice: Money   (subtotal derivado)

AuditableEntity (interface)  ← implementada por Product, Order, InventoryItem, Warehouse, Seller, User
Operation : operationId, operationType: OperationType, executionDate, performedBy: User, affectedEntity: AuditableEntity
AuditLog  : auditId, operationType: OperationType, operationDate, performedBy: User, userRole: UserRole,
            affectedEntity: AuditableEntity, severity: AuditSeverity, details: Map<String,Object>
```

Regla: **relaciones por objeto**, no por IDs (romper `Order.buyerId`, `OrderItem.productId`).

### B3 · Excepciones → *fase 2*

Alinear con `bank`: `DomainException` (base `RuntimeException`), `EntityNotFoundException`,
`InvalidStatusTransitionException`, `UnauthorizedOperationException`. Específicas de
NexusMarket: `NegativeStockException`, `InvalidReservationException` (inventario
inexistente o `DAMAGED`), `FinalizedOrderModificationException` (§11), `DuplicateUserException`
(documento/email únicos, §11), `PaymentRejectedException`.
Mapear las actuales: `DomainValidationException`→`DomainException`, `ResourceNotFoundException`→`EntityNotFoundException`.

### B4 · Puertos de salida — `application.domain.ports.out` → *fase 4*

Firmas que reciben **modelos de dominio**:

- `UserRepositoryPort` (save, findByUsername(User), findByIdentification(User), existsByEmail(User), existsByIdentification(User), update)
- `BuyerRepositoryPort`, `SellerRepositoryPort`, `WarehouseRepositoryPort`
- `ProductRepositoryPort` (save, findByIdentifier(Product), findBySeller(Seller), findByStatus(Product), update)
- `InventoryRepositoryPort` (save(InventoryItem), findByProductAndWarehouse(InventoryItem), saveMovement(InventoryMovement), findMovements(InventoryItem))
- `CartRepositoryPort`, `OrderRepositoryPort` (save, findByIdentifier(Order), findByBuyer(Buyer), update)
- `OperationRepositoryPort`, `AuditLogRepositoryPort` (Mongo previsto)
- `PaymentGatewayPort` → `PaymentResult process(Order order)` (simulación/fallo en adapter, fase 5)
- `NotificationPort` → `void send(Notification notification)` (crear modelo `Notification` — el profe lo dejó pendiente)
- `PasswordServicePort`, `JwtServicePort` (declarar ya, uso en fase 6)
- `BusinessConfigurationPort` (p. ej. minutos de expiración de pedido no pagado — fase 7)

### B5 · Puertos de entrada — `application.domain.ports.in` → *fase 4*

Una interfaz de caso de uso por servicio (el profe las documenta pero no las creó;
aquí sí, porque estaban en la fase 4 original). Método corto (`register`, `checkout`,
`reserve`, `dispatch`...). Los `*Service` las implementan.

### B6 · Servicios de dominio — `application.domain.services.<subdominio>` → *fase 3–4*

`@Service` + `@RequiredArgsConstructor`, método `execute(...)`, componen
`RegisterOperationAndAuditService` y los servicios de autorización.

| Subdominio | Servicios |
|---|---|
| `user/` | RegisterUserService (SELLER solo por ADMIN — Dominio 3/RG03), ChangeUserStatusService, ConsultUserService, LoginService (interfaz ya; lógica fase 6) |
| `buyer/` | RegisterBuyerService (auto-registro permitido — Dominio 2), UpdateBuyerService, ChangeBuyerCommercialStatusService, ConsultBuyerService |
| `seller/` | RegisterSellerService (solo ADMIN; crea vendedor + **primera bodega** — flujo 6.1.1), ConsultSellerService |
| `warehouse/` | RegisterWarehouseService (ADMIN; tipo MARKETPLACE/SELLER), ConsultWarehouseService |
| `catalog/` | PublishProductService (solo SELLER — matriz §12), UpdateProductService, ChangeProductStatusService, AddProductVariantService, ConsultCatalogService |
| `inventory/` | RegisterInventoryEntryService, ReserveInventoryService (no reserva inexistente ni `DAMAGED`; no negativo — Dominio 6 + §11), ReleaseReservationService, AdjustInventoryService, RegisterInventoryReturnService, ConsultInventoryService — cada uno crea `InventoryMovement` + Operation/Audit |
| `cart/` | AddCartItemService, RemoveCartItemService, ConsultCartService, CheckoutCartService (Cart → Order `PENDING_PAYMENT`) |
| `order/` | ProcessOrderPaymentService (usa `PaymentGatewayPort`; `PAID`; físico→espera despacho, digital→`DELIVERED`), DispatchOrderService (LOGISTICS_OPERATOR; `PAID`→`DISPATCHED`), ConfirmDeliveryService (`DISPATCHED`→`DELIVERED`), ConsultOrderService. Guard: pedido `DELIVERED` inmutable (§11) |
| `authorization/` | ValidateRoleAuthorizationService, ValidateUserStatusService, AuthorizeSellerOnboardingService, AuthorizeProductManagementService, AuthorizeOrderOperationService, ValidateOwnershipService (comprador no accede a datos de otros — Dominio 2, RG03) |
| `operation/` | RegisterOperationService, RegisterAuditLogService, RegisterOperationAndAuditService, ConsultOperationsService, ConsultAuditLogService |

**Retirar**: `PaymentSimulationDomainService` (→ adapter), `OrderCheckoutDomainService`
e `InventoryDomainService` (→ servicios por subdominio), `domain/event/*` (el profe no
usa eventos; usa Operation/Audit).

**Retirar capa vieja**: `application/dto/*` (vuelven en fase 6 como response DTOs REST),
`application/usecase/*` (→ services), `application/port/*` (→ `domain/ports`).

### B7 · Wiring

Con `@Service` en cada servicio y raíz `application`, Spring cablea solo; **no** hace
falta clase de `@Bean`. Los puertos `out` no tienen adapter hasta fase 5, así que
`@SpringBootTest contextLoads` **no podrá levantar el contexto** (mismo caso que `bank`).
→ En fases 2–4 los servicios se prueban **sin Spring** (constructor directo). Posponer
`@SpringBootTest` a fase 5, o marcarlo `@Disabled` con comentario.

---

## Verificación (fases 2–4)

- `./mvnw clean compile` OK.
- Tests unitarios de dominio **puros** (sin Spring), por subdominio:
  - RegisterUserService: SELLER con solicitante ≠ ADMIN lanza `UnauthorizedOperationException`.
  - Documento/email duplicado lanza `DuplicateUserException`.
  - ReserveInventoryService: inventario inexistente / `DAMAGED` / stock insuficiente → excepción; caso OK descuenta y crea `InventoryMovement`.
  - Order: transición inválida de estado → `InvalidStatusTransitionException`; `DELIVERED` inmutable.
  - Cada acción significativa invoca `RegisterOperationAndAuditService` (verificable con puerto en test doble).
- `./mvnw test` verde (con `@SpringBootTest` posiblemente `@Disabled` hasta fase 5, documentado).
- Cada servicio del código tiene su sección en `SDD/domain/services/*`.

---

## Orden de ejecución

| Fase (corrección) | SDD primero | Luego código |
|---|---|---|
| **1** | `Software Architecture.md` | `pom.xml`, paquetes raíz `application`, `infrastructure/config`, `NexusMarketApplication` |
| **2** | `Domain Model.md`, `Domain Value Objects.md` | `DomainCatalog` + catálogos + enums + VOs + modelos + excepciones |
| **3** | `Domain Services.md` + `services/*.md` | servicios por subdominio + Operation/Audit + authorization |
| **4** | `Output Ports.md`, `Input Ports.md` | `ports/out`, `ports/in`, retiro de `dto/`+`usecase/`+`port/`+`event/`, wiring |

Después: continuar con la **fase 5** original (persistencia) implementando los nuevos
puertos, incluido el adapter de simulación de pago (`PaymentGatewayPort`) y el adapter
de auditoría en Mongo.

---

## Riesgos / notas

- **Refactor grande**: rompe casi todos los imports. Hacerlo en **rama dedicada**,
  subdominio por subdominio, commiteando el SDD de cada subdominio antes que su código.
- `@SpringBootTest` se rompe sin adapters (igual que `bank`). Dominio 100% testeable sin Spring.
- `Money`/`Email`/`DocumentId`/`StockQuantity`: `market_place` los tiene y superan a los
  primitivos de `bank`; se conservan añadiendo igualdad por valor.
- Nombre: unificar en **NexusMarket** en SDD y README.
- `ProductType` PHYSICAL/DIGITAL se modela como `DomainCatalog` (no enum) por su carga
  semántica de fulfillment y presencia en catálogo/reportes; si se prefiere enum plano,
  es el único catálogo discutible.
