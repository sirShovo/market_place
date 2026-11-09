# Plan de Implementacion: API NexusMarket (Hexagonal + DDD)

Este documento detalla el plan de accion exhaustivo para construir la API de NexusMarket en Spring Boot. Combina la rigurosidad arquitectonica (Hexagonal + DDD) basada en el proyecto de referencia (`bank`), las decisiones de negocio previas y las directrices de la Especificacion Funcional oficial ([`SPECS.md`](SPECS.md)).

> **Nota de alineacion:** las Fases 1 a 5 estan completadas y alineadas con las convenciones del proyecto de referencia. Ver [`ALIGNMENT_PLAN.md`](ALIGNMENT_PLAN.md) para el detalle del proceso de alineacion. La documentacion SDD reside en `SDD/Software Architecture/`, `SDD/domain/` y `SDD/adapters/`, junto con [`SDD/Code Walkthrough and Role Flows.md`](SDD/Code%20Walkthrough%20and%20Role%20Flows.md).

## Decisiones y Acuerdos de Diseno
- **Estructura Estricta (`application` como paquete raiz):** Se respeta la separacion de paquetes alineada con el proyecto de referencia:
  - `application.domain` (`models`, `valueobjects`, `enums`, `exceptions`, `services.<subdominio>`, `ports.in`, `ports.out`).
  - `application.adapters.in.rest` (`controllers`, `requests`, `responses`, `mappers`) y `application.adapters.in.scheduler`.
  - `application.adapters.out.persistence.mysql` (`adapters`, `entities`, `repositories`, `mappers`), `application.adapters.out.persistence.mongodb` (`adapters`, `documents`, `repositories`, `mappers`), `application.adapters.out.payment` y `application.adapters.out.notification`.
  - `application.infrastructure` (`config`, `database`, `security`).
- **Documentacion SDD:** Toda la documentacion de diseno (Software Design Document) se redacta en ingles dentro de la carpeta `SDD/` antes de implementar el codigo de cada fase.
- **Simulacion de Pagos:** El dominio define el puerto `PaymentGatewayPort`; la simulacion con probabilidad de fallo y soporte de reintentos reside en el adaptador de salida (`SimulatedPaymentAdapter`, Fase 5).
- **Trazabilidad y Auditoria:** Toda accion significativa del negocio registra una `Operation` (persistencia relacional en MySQL) y un `AuditLog` inmutable (persistencia documental en MongoDB) mediante `RegisterOperationAndAuditService`.
- **Manejo de Base de Datos:** Las tablas relacionales se gestionan desde las Entidades JPA mediante `spring.jpa.hibernate.ddl-auto=update`.
- **Notificaciones:** El envio de notificaciones se abstrae mediante `NotificationPort` y se implementa con `EmailNotificationAdapter` (`JavaMailSender`).
- **Logica Especifica de Negocio (`SPECS.md`):**
  - Division entre productos Fisicos (`PhysicalProduct`, requieren inventario y despacho) y Digitales (`DigitalProduct`, entrega inmediata tras pago).
  - Trazabilidad estricta de inventario: `InventoryItem` vinculado a `(Product, Warehouse)` e `InventoryMovement` para rastrear ingresos (`ENTRY`), reservas (`RESERVATION`), salidas por venta (`SALE_EXIT`), ajustes (`ADJUSTMENT`) y devoluciones (`RETURN`). No se permiten existencias negativas ni reservar inventario inexistente o `DAMAGED` (§11).
  - Unicidad de documento (`DocumentId`), correo (`Email`) y nombre de usuario en toda la plataforma (`User`, `Buyer`, `Seller` — §11).
  - Vendedores solo pueden ser incorporados por un `ADMIN` junto con su primera bodega (`OnboardSellerService`). Los compradores se auto-registran (`RegisterBuyerService`). Ambos flujos crean su cuenta `User` de autenticacion vinculada por `identification`.
  - Pedidos finalizados (`DELIVERED`) son inmutables (§11).

---

## Fases de Implementacion

### Fase 1: Configuracion Base, Arquitectura Inicial y SDD (Entregable 1)
_Objetivo: Estructurar el cascaron del proyecto bajo el paquete raiz `application` y preparar la base de la documentacion arquitectonica._

- [x] 1.1. Inicializar y limpiar `pom.xml` con Spring Boot 4.1.0 (Data JPA, Data MongoDB, Security, WebMVC, Validation, Mail, OpenAPI, JJWT, MySQL, Lombok).
- [x] 1.2. Configurar `application.properties` (conexion a MySQL, MongoDB, variables JWT, dialecto de Hibernate, credenciales SMTP).
- [x] 1.3. Crear estructura de paquetes Hexagonal con raiz `application` (`NexusMarketApplication`, `application.domain`, `application.infrastructure.config`).
- [x] 1.4. Implementar utilidades de infraestructura y configuraciones globales:
  - [x] 1.4.1. `GlobalExceptionHandler` y envoltorio de respuesta unificado `ApiResponse`.
  - [x] 1.4.2. `JacksonConfig` para serializacion de fechas y objetos.
- [x] 1.5. Redactar [`SDD/Software Architecture/Software Architecture.md`](SDD/Software%20Architecture/Software%20Architecture.md) en ingles.

### Fase 2: Modelado del Dominio - Estructura y Reglas (Entregable 2)
_Objetivo: Disenar el nucleo del negocio sin dependencias tecnologicas en `application.domain.{models,valueobjects,enums,exceptions}`._

- [x] 2.1. **Value Objects y Catalogos (`application.domain.valueobjects` y `enums`):**
  - [x] 2.1.1. `DomainCatalog` abstracto (igualdad por `code`) y 12 catalogos controlados: `UserRole`, `UserStatus`, `BuyerCommercialStatus`, `SellerStatus`, `WarehouseType`, `ProductType`, `ProductStatus`, `OrderStatus`, `CartStatus`, `InventoryMovementType`, `InventoryItemCondition`, `OperationType`.
  - [x] 2.1.2. Value Objects primitivos (`record` con validacion): `Money`, `Email`, `DocumentId`, `StockQuantity` (validacion de enteros no negativos).
  - [x] 2.1.3. Enums tecnicos (`application.domain.enums`): `PaymentResult`, `NotificationChannel`, `AuditSeverity`.
- [x] 2.2. **Modelos de Dominio (`application.domain.models`):**
  - [x] 2.2.1. Jerarquia `Person` (abstracta) -> `User`, `Buyer`, `Seller`.
  - [x] 2.2.2. Jerarquia `Product` (abstracta) -> `PhysicalProduct`, `DigitalProduct`, mas `Category` y `ProductVariant`.
  - [x] 2.2.3. `Warehouse`, `InventoryItem` y `InventoryMovement`.
  - [x] 2.2.4. `Cart`, `CartItem`, `Order` (con invariantes de ciclo de vida e inmutabilidad en `DELIVERED`) y `OrderItem` (con referencia a `Warehouse` de cumplimiento).
  - [x] 2.2.5. `AuditableEntity`, `Operation`, `AuditLog` y `Notification`.
- [x] 2.3. **Excepciones de Dominio (`application.domain.exceptions`):** `DomainException`, `EntityNotFoundException`, `InvalidStatusTransitionException`, `UnauthorizedOperationException`, `NegativeStockException`, `InvalidReservationException`, `FinalizedOrderModificationException`, `DuplicateUserException`, `PaymentRejectedException`.
- [x] 2.4. Documentar el modelo de dominio y objetos de valor en [`SDD/domain/Domain Model.md`](SDD/domain/Domain%20Model.md) y [`SDD/domain/Domain Value Objects.md`](SDD/domain/Domain%20Value%20Objects.md).

### Fase 3: Servicios de Dominio, Autorizacion y Auditoria (Entregable 3)
_Objetivo: Implementar las reglas de negocio por caso de uso en `application.domain.services.<subdominio>`, componiendo autorizacion y trazabilidad (`Operation` + `AuditLog`)._

- [x] 3.1. **Colaboradores Transversales (`authorization`, `operation`, `identity`):**
  - [x] 3.1.1. Autorizacion (`application.domain.services.authorization`): `ValidateUserStatusService`, `ValidateRoleAuthorizationService`, `ValidateBuyerOwnershipService`, `ValidateSellerOwnershipService`, `ValidateProductOwnershipService`, `ValidateOrderAccessService`, `ValidateBuyerOwnsOrderService`, `ValidateBuyerCanPurchaseService`.
  - [x] 3.1.2. Unicidad en plataforma (`application.domain.services.identity`): `ValidatePlatformUniquenessService` (verifica `DocumentId` y `Email` entre `User`, `Buyer` y `Seller`).
  - [x] 3.1.3. Operaciones y Auditoria (`application.domain.services.operation`): `RegisterOperationService`, `RegisterAuditLogService`, `RegisterOperationAndAuditService`, `ConsultAuditLogService`.
- [x] 3.2. **Servicios de Caso de Uso por Subdominio (`application.domain.services.*`):**
  - [x] 3.2.1. `user/`: `RegisterUserService`, `ChangeUserStatusService`, `ConsultUserService`.
  - [x] 3.2.2. `buyer/`: `RegisterBuyerService` (crea `Buyer` + `User` de login), `UpdateBuyerService`, `ChangeBuyerCommercialStatusService`, `ConsultBuyerService`.
  - [x] 3.2.3. `seller/`: `OnboardSellerService` (solo `ADMIN`; crea `Seller` + primera `Warehouse` + `User` de login), `ConsultSellerService` (permite autogestion del `SELLER` y consulta por `ADMIN`/`SUPERVISOR`).
  - [x] 3.2.4. `warehouse/`: `RegisterWarehouseService`, `ConsultWarehouseService`.
  - [x] 3.2.5. `catalog/`: `PublishProductService`, `UpdateProductService`, `ChangeProductStatusService`, `ConsultCatalogService`, `ConsultSellerProductsService`.
  - [x] 3.2.6. `inventory/`: `RegisterInventoryEntryService`, `ReserveInventoryService`, `ReserveStockForOrderItemService` (colaborador interno de checkout), `ReleaseReservationService`, `AdjustInventoryService`, `RegisterInventoryReturnService`, `ConsultInventoryService`.
  - [x] 3.2.7. `cart/`: `AddCartItemService`, `RemoveCartItemService`, `ClearCartService`, `ConsultCartService`.
  - [x] 3.2.8. `order/`: `CheckoutCartService` (convierte carrito a orden y reserva stock fisico), `ProcessOrderPaymentService` (valida propiedad del comprador, invoca `PaymentGatewayPort`, entrega inmediata si es digital y notifica), `DispatchOrderService` (transiciona a `DISPATCHED` y registra movimientos `SALE_EXIT` por linea fisica), `ConfirmDeliveryService`, `ConsultOrderService`, `ConsultBuyerOrdersService`.
- [x] 3.3. Documentar todos los servicios en [`SDD/domain/Domain Services.md`](SDD/domain/Domain%20Services.md) y [`SDD/domain/services/*.md`](SDD/domain/services/).

### Fase 4: Puertos de Entrada y Salida del Dominio (Entregable 4)
_Objetivo: Definir los contratos de entrada y salida en `application.domain.ports.{in,out}` trabajando exclusivamente con modelos de dominio y retirar la capa legacy._

- [x] 4.1. **Input Ports (`application.domain.ports.in` - 33 interfaces):** Un contrato `<Verb><Noun>UseCase` por cada servicio de caso de uso (`RegisterUserUseCase`, `ChangeUserStatusUseCase`, `ConsultUserUseCase`, `RegisterBuyerUseCase`, `UpdateBuyerUseCase`, `ChangeBuyerCommercialStatusUseCase`, `ConsultBuyerUseCase`, `OnboardSellerUseCase`, `ConsultSellerUseCase`, `RegisterWarehouseUseCase`, `ConsultWarehouseUseCase`, `PublishProductUseCase`, `UpdateProductUseCase`, `ChangeProductStatusUseCase`, `ConsultCatalogUseCase`, `ConsultSellerProductsUseCase`, `RegisterInventoryEntryUseCase`, `ReserveInventoryUseCase`, `ReleaseReservationUseCase`, `AdjustInventoryUseCase`, `RegisterInventoryReturnUseCase`, `ConsultInventoryUseCase`, `AddCartItemUseCase`, `RemoveCartItemUseCase`, `ClearCartUseCase`, `ConsultCartUseCase`, `CheckoutCartUseCase`, `ProcessOrderPaymentUseCase`, `DispatchOrderUseCase`, `ConfirmDeliveryUseCase`, `ConsultOrderUseCase`, `ConsultBuyerOrdersUseCase`, `ConsultAuditLogUseCase`).
- [x] 4.2. **Output Ports (`application.domain.ports.out` - 16 interfaces):**
  - [x] 4.2.1. Puertos de repositorio (11): `UserRepositoryPort`, `BuyerRepositoryPort`, `SellerRepositoryPort`, `WarehouseRepositoryPort`, `CategoryRepositoryPort`, `ProductRepositoryPort`, `InventoryRepositoryPort`, `CartRepositoryPort`, `OrderRepositoryPort`, `OperationRepositoryPort`, `AuditLogRepositoryPort`.
  - [x] 4.2.2. Puertos de servicio (5): `PaymentGatewayPort`, `NotificationPort`, `PasswordServicePort`, `JwtServicePort`, `BusinessConfigurationPort`.
- [x] 4.3. Retirar paquetes legacy (`application.dto`, `application.usecase`, `application.port`, `domain.event`); los DTOs de transporte HTTP pertenecen exclusivamente a `application.adapters.in.rest` en la Fase 6.
- [x] 4.4. Pruebas unitarias puras del dominio (`src/test/java/application/domain/**`) verificando reglas criticas sin contexto de Spring.
- [x] 4.5. Documentar puertos y recorridos por rol en [`SDD/domain/Input Ports.md`](SDD/domain/Input%20Ports.md), [`SDD/domain/Output Ports.md`](SDD/domain/Output%20Ports.md) y [`SDD/Code Walkthrough and Role Flows.md`](SDD/Code%20Walkthrough%20and%20Role%20Flows.md).

### Fase 5: Persistencia e Infraestructura de Salida (Adaptadores de Salida)
_Objetivo: Implementar los adaptadores de salida en `application.adapters.out` para MySQL, MongoDB, pasarela de pago simulada, notificaciones y configuracion._

- [x] 5.1. **Persistencia Relacional MySQL (`application.adapters.out.persistence.mysql`):**
  - [x] 5.1.1. Entidades JPA (`entities/`): `UserJpaEntity`, `BuyerJpaEntity`, `SellerJpaEntity`, `WarehouseJpaEntity`, `CategoryJpaEntity`, `ProductJpaEntity` (soporte para `PhysicalProduct` y `DigitalProduct` + `ProductVariantEmbeddable`), `InventoryItemJpaEntity`, `InventoryMovementJpaEntity`, `CartJpaEntity`, `CartItemJpaEntity`, `OrderJpaEntity`, `OrderItemJpaEntity`, `OperationJpaEntity`.
  - [x] 5.1.2. Repositorios Spring Data JPA (`repositories/`): interfaces `*JpaRepository` para cada entidad (`UserJpaRepository`, `BuyerJpaRepository`, `SellerJpaRepository`, `WarehouseJpaRepository`, `CategoryJpaRepository`, `ProductJpaRepository`, `InventoryItemJpaRepository`, `InventoryMovementJpaRepository`, `CartJpaRepository`, `OrderJpaRepository`, `OperationJpaRepository`).
  - [x] 5.1.3. Mappers de persistencia (`mappers/`): conversion explicita entre Modelos de Dominio (y `DomainCatalog` / Value Objects) y Entidades JPA (`UserMapper`, `BuyerMapper`, `SellerMapper`, `WarehouseMapper`, `CategoryMapper`, `ProductMapper`, `InventoryMapper`, `CartMapper`, `OrderMapper`, `OperationMapper`).
  - [x] 5.1.4. Adaptadores MySQL (`adapters/`): implementaciones `@Component` de los 10 puertos de repositorio relacionales (`UserMySqlAdapter`, `BuyerMySqlAdapter`, `SellerMySqlAdapter`, `WarehouseMySqlAdapter`, `CategoryMySqlAdapter`, `ProductMySqlAdapter`, `InventoryMySqlAdapter`, `CartMySqlAdapter`, `OrderMySqlAdapter`, `OperationMySqlAdapter`).
- [x] 5.2. **Persistencia Documental MongoDB (`application.adapters.out.persistence.mongodb`):**
  - [x] 5.2.1. `AuditLogDocument`, `AuditLogMongoRepository`, `AuditLogDocumentMapper` y `AuditLogMongoAdapter` (implementando `AuditLogRepositoryPort`).
- [x] 5.3. **Adaptadores de Servicio Externo (`application.adapters.out`):**
  - [x] 5.3.1. `SimulatedPaymentAdapter` (`application.adapters.out.payment`) implementando `PaymentGatewayPort` con aprobacion/rechazo probabilistico configurable para probar reintentos.
  - [x] 5.3.2. `EmailNotificationAdapter` (`application.adapters.out.notification`) implementando `NotificationPort` mediante `JavaMailSender` (con fallback seguro si SMTP no esta activo en entorno local/test).
  - [x] 5.3.3. `PropertiesConfigurationAdapter` (`application.infrastructure.config`) implementando `BusinessConfigurationPort`.
- [x] 5.4. Documentar la capa de persistencia y adaptadores de salida en [`SDD/adapters/Persistence and Output Adapters.md`](SDD/adapters/Persistence%20and%20Output%20Adapters.md) (en ingles) y validar pruebas de adaptadores.

### Fase 6: Controladores REST y Seguridad (Adaptadores de Entrada)
_Objetivo: Implementar autenticacion JWT, seguridad por roles y exponer la API REST en `application.adapters.in.rest` y `application.infrastructure.security`._

- [ ] 6.1. **Seguridad y Autenticacion (`application.infrastructure.security` + `domain`):**
  - [ ] 6.1.1. Implementar `BCryptPasswordAdapter` (`PasswordServicePort`) y `JjwtAdapter` (`JwtServicePort`).
  - [ ] 6.1.2. Implementar caso de uso de autenticacion (`LoginUseCase` / `LoginService`).
  - [ ] 6.1.3. Configurar `JwtAuthenticationFilter` y `SecurityConfig` aplicando la matriz de responsabilidades y resolviendo el `User` autenticado.
- [ ] 6.2. **Capa REST (`application.adapters.in.rest`):**
  - [ ] 6.2.1. Request DTOs (`requests/`), Response DTOs (`responses/`) y Mappers REST (`mappers/`) para traducir entre HTTP y Modelos de Dominio.
  - [ ] 6.2.2. Controladores (`controllers/`): `AuthController`, `UserController`, `BuyerController`, `SellerController`, `WarehouseController`, `ProductController`, `InventoryController`, `CartController`, `OrderController`, `AuditController`.
- [ ] 6.3. Configurar OpenAPI / Swagger UI y habilitar `@SpringBootTest` (`NexusMarketApplicationTests`).
- [ ] 6.4. Documentar los endpoints REST y el flujo de seguridad en `SDD/`.

### Fase 7: Schedulers, Semilla de Datos y Refinamientos Finales
_Objetivo: Anadir automatizaciones operativas, bootstrap del administrador inicial y pulido final de la documentacion._

- [ ] 7.1. Implementar `OrderExpirationScheduler` (`application.adapters.in.scheduler`) para liberar reservas de ordenes en `PENDING_PAYMENT` expiradas segun `BusinessConfigurationPort`.
- [ ] 7.2. Implementar `DataSeeder` (`application.infrastructure.config`) para asegurar la creacion idempotente de un usuario `ADMIN` inicial que permita incorporar vendedores y otros usuarios.
- [ ] 7.3. Pruebas integrales del flujo completo y revision final de toda la documentacion SDD en ingles.
