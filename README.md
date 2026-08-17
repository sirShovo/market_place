# MarketPlace API — Arquitectura Hexagonal + DDD + Spring Boot

Documento de referencia arquitectonica y diseno estructural para el desarrollo de la API del Market Place, basado en el proyecto de referencia bancario ([SantiagoMacias](file:///c:/Users/Shovo/Documents/me/tdea/CSoftware2SantiagoMacias/CSoftware2SantiagoMacias-develop/SantiagoMacias)).

---

## 1. Analisis del Proyecto de Referencia

El proyecto base es un sistema de gestion bancaria desarrollado bajo los principios de Arquitectura Hexagonal (Puertos y Adaptadores) combinados con Domain-Driven Design (DDD).

### 1.1 Tecnologias y Herramientas Identificadas

| Categoria | Tecnologia / Libreria | Version / Detalle |
|---|---|---|
| Lenguaje | Java | 17 (LTS) |
| Framework Core | Spring Boot | 4.0.6 (Starters: Web, Data JPA, Security, Validation) |
| Seguridad | Spring Security + JJWT | io.jsonwebtoken (jjwt-api, jjwt-impl, jjwt-jackson 0.12.5) + BCrypt |
| Base de Datos | MySQL | mysql-connector-j / Hibernate ORM |
| Documentacion API | SpringDoc OpenAPI | springdoc-openapi-starter-webmvc-ui 2.8.8 (Swagger UI) |
| Serializacion | Jackson | jackson-databind + datatype-jsr310 (2.17.2) |
| Tareas Programadas | Spring Scheduling | @EnableScheduling (para expiracion automatica de transacciones) |
| Construccion | Apache Maven | Maven Wrapper (mvnw) |

### 1.2 Sistema de Autenticacion y Seguridad

El proyecto cuenta con un modulo completo de autenticacion y autorizacion:
- **Endpoints Publicos:** `/api/auth/login`, `/api/auth/register`, `/swagger-ui/**`, `/v3/api-docs/**`.
- **Mecanismo de Login:** Recibe identificacion y contrasena en [AuthController.java](file:///c:/Users/Shovo/Documents/me/tdea/CSoftware2SantiagoMacias/CSoftware2SantiagoMacias-develop/SantiagoMacias/src/main/java/com/bank/adapter/in/web/controller/AuthController.java), valida credenciales cifradas con BCrypt y retorna un token JWT firmado (HMAC-SHA256).
- **Filtro de Intercepcion:** [JwtAuthenticationFilter.java](file:///c:/Users/Shovo/Documents/me/tdea/CSoftware2SantiagoMacias/CSoftware2SantiagoMacias-develop/SantiagoMacias/src/main/java/com/bank/config/JwtAuthenticationFilter.java) intercepta cada peticion, valida el token y carga el contexto del usuario autenticado en Spring Security.
- **Control de Acceso:** Basado en roles (RBAC) mediante metodos utilitarios como `SecurityContextHelper.requireAnyRole(...)`.

### 1.3 Persistencia y Base de Datos

- **Conexion:** Configurada hacia MySQL en `application.properties` (`jdbc:mysql://localhost:3306/bankdb`).
- **Desacoplamiento DDD:** El dominio no conoce JPA ni anotaciones de base de datos. La infraestructura define entidades JPA (`JpaEntity`), interfaces Spring Data JPA (`JpaRepository`), mapeadores (`PersistenceMapper`) y adaptadores (`PersistenceAdapter`) que implementan los puertos de salida (`RepositoryPort`).

---

## 2. Mapa Arquitectonico Hexagonal + DDD

El flujo de control y las capas se organizan de la siguiente manera:

```
+-------------------------------------------------------------------------+
| DRIVING ADAPTERS / ADAPTERS IN (adapter/in)                             |
| - REST Controllers (web/controller): Endpoints HTTP, mapeo DTOs         |
| - Schedulers (scheduler): Tareas automaticas y cron jobs                |
+------------------------------------+------------------------------------+
                                     | Invoca Puertos de Entrada (Input Ports)
+------------------------------------v------------------------------------+
| APPLICATION LAYER (application)                                         |
| - Use Cases (application/usecase): Orquestacion de flujos               |
| - Input Ports (application/port/input): Interfaces de casos de uso      |
| - Output Ports (application/port/output): Interfaces de persistencia/ext|
| - DTOs (application/dto): Objetos de transferencia Request/Response     |
+------------------------------------+------------------------------------+
                                     | Opera sobre el Dominio
+------------------------------------v------------------------------------+
| DOMAIN LAYER (domain)  (Cero dependencias de Frameworks/Spring/JPA)     |
| - Aggregates (domain/model/aggregate): Entidades raiz de consistencia   |
| - Entities (domain/model/entity): Entidades con identidad propia        |
| - Value Objects (domain/model/valueobject): Objetos inmutables sin ID   |
| - Domain Services (domain/service): Logica que involucra varios modelos |
| - Domain Events (domain/event o event/): Eventos del negocio            |
| - Domain Exceptions (domain/exception): Reglas de negocio violadas      |
+------------------------------------+------------------------------------+
                                     ^ Implementa Puertos de Salida (Output Ports)
+------------------------------------+------------------------------------+
| DRIVEN ADAPTERS / ADAPTERS OUT (adapter/out)                            |
| - Persistence Adapters (persistence): Implementacion de puertos         |
| - JPA Entities (persistence/entity): Esquema relacional ORM             |
| - Spring Data Repositories (persistence/repository): Interfaces CRUD    |
| - Mappers (persistence/mapper): Conversion Dominio <-> JPA Entity       |
+-------------------------------------------------------------------------+
| INFRASTRUCTURE & CROSS-CUTTING (config, shared)                         |
| - SecurityConfig, JwtTokenProvider, JwtAuthenticationFilter             |
| - GlobalExceptionHandler, ApiResponse Wrapper                           |
+-------------------------------------------------------------------------+
```

---

## 3. Estructura de Paquetes para Market Place

Estructura de paquetes recomendada para el proyecto `market_place`:

```
com.marketplace/
|-- MarketPlaceApplication.java
|-- adapter/
|   |-- in/
|   |   |-- web/
|   |   |   |-- controller/
|   |   |   |   |-- AuthController.java
|   |   |   |   |-- UserController.java
|   |   |   |   |-- ProductController.java
|   |   |   |   |-- CategoryController.java
|   |   |   |   |-- OrderController.java
|   |   |   |   |-- ReviewController.java
|   |   |   |   |-- AuditLogController.java
|   |   |-- scheduler/
|   |       |-- OrderExpirationScheduler.java
|   |-- out/
|       |-- persistence/
|           |-- UserPersistenceAdapter.java
|           |-- ProductPersistenceAdapter.java
|           |-- OrderPersistenceAdapter.java
|           |-- CategoryPersistenceAdapter.java
|           |-- AuditLogPersistenceAdapter.java
|           |-- entity/
|           |   |-- UserJpaEntity.java
|           |   |-- ProductJpaEntity.java
|           |   |-- OrderJpaEntity.java
|           |   |-- OrderItemJpaEntity.java
|           |   |-- CategoryJpaEntity.java
|           |   |-- AuditLogJpaEntity.java
|           |-- mapper/
|           |   |-- UserPersistenceMapper.java
|           |   |-- ProductPersistenceMapper.java
|           |   |-- OrderPersistenceMapper.java
|           |   |-- CategoryPersistenceMapper.java
|           |   |-- AuditLogPersistenceMapper.java
|           |-- repository/
|               |-- UserJpaRepository.java
|               |-- ProductJpaRepository.java
|               |-- OrderJpaRepository.java
|               |-- CategoryJpaRepository.java
|               |-- AuditLogJpaRepository.java
|-- application/
|   |-- dto/
|   |   |-- UserDto.java
|   |   |-- ProductDto.java
|   |   |-- OrderDto.java
|   |   |-- CategoryDto.java
|   |   |-- CommonDto.java
|   |-- port/
|   |   |-- input/
|   |   |   |-- UserInputPort.java
|   |   |   |-- ProductInputPort.java
|   |   |   |-- OrderInputPort.java
|   |   |   |-- CategoryInputPort.java
|   |   |   |-- AuditLogInputPort.java
|   |   |-- output/
|   |       |-- UserRepositoryPort.java
|   |       |-- ProductRepositoryPort.java
|   |       |-- OrderRepositoryPort.java
|   |       |-- CategoryRepositoryPort.java
|   |       |-- AuditLogRepositoryPort.java
|   |-- usecase/
|       |-- UserUseCase.java
|       |-- ProductUseCase.java
|       |-- OrderUseCase.java
|       |-- CategoryUseCase.java
|       |-- AuditLogUseCase.java
|-- domain/
|   |-- event/
|   |   |-- DomainEvent.java
|   |   |-- OrderCreatedEvent.java
|   |   |-- OrderPaidEvent.java
|   |   |-- OrderCancelledEvent.java
|   |   |-- StockDepletedEvent.java
|   |-- exception/
|   |   |-- DomainValidationException.java
|   |   |-- ResourceNotFoundException.java
|   |   |-- InsufficientStockException.java
|   |   |-- InvalidOrderStateException.java
|   |   |-- AccessDeniedException.java
|   |-- model/
|   |   |-- aggregate/
|   |   |   |-- Order.java
|   |   |   |-- Product.java
|   |   |-- entity/
|   |   |   |-- User.java
|   |   |   |-- Category.java
|   |   |   |-- OrderItem.java
|   |   |   |-- AuditLog.java
|   |   |-- valueobject/
|   |       |-- Money.java
|   |       |-- Email.java
|   |       |-- PhoneNumber.java
|   |       |-- OrderStatus.java
|   |       |-- ProductStatus.java
|   |       |-- UserRole.java (ADMIN, SELLER, BUYER)
|   |       |-- UserStatus.java
|   |       |-- StockQuantity.java
|   |-- service/
|       |-- OrderCheckoutDomainService.java
|       |-- ProductStockDomainService.java
|-- config/
|   |-- SecurityConfig.java
|   |-- JwtTokenProvider.java
|   |-- JwtAuthenticationFilter.java
|   |-- GlobalExceptionHandler.java
|   |-- JacksonConfig.java
|-- shared/
    |-- SecurityContextHelper.java
    |-- OrderNumberGenerator.java
```

---

## 4. Equivalencias de Dominio: Banco vs Market Place

| Concepto en Sistema Bancario | Equivalente en Marketplace | Descripcion en Marketplace |
|---|---|---|
| `User` (Analyst, Teller, Client) | `User` (Admin, Seller, Buyer) | Gestion de perfiles, registro y roles de plataforma |
| `BankAccount` (Cuenta bancaria) | `Product` / `Inventory` | Catalogo de articulos disponibles para la venta |
| `Loan` (Prestamo y aprobacion) | `Order` (Orden y aprobacion de pago) | Ciclo de vida: Creacion, Pago, Envio, Entrega, Cancelacion |
| `Transfer` (Transferencia) | `PaymentTransaction` | Procesamiento del cobro y liquidacion entre comprador y vendedor |
| `AuditLog` (Bitacora de auditoria) | `AuditLog` | Trazabilidad de operaciones criticas (cambios de precio, compras, cancelaciones) |
| `TransferExpiryScheduler` | `OrderExpirationScheduler` | Cancelacion automatica de ordenes pendientes de pago tras X tiempo |
