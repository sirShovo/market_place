# Plan de Implementación: API Market Place (Hexagonal + DDD)

Este documento detalla el plan de acción exhaustivo para construir la API del Market Place en Spring Boot. Combina la rigurosidad arquitectónica (Hexagonal + DDD) basada en el proyecto de referencia de Santiago, las decisiones de negocio previas, y las nuevas directrices de la Especificación Funcional oficial (PDF).

## Decisiones y Acuerdos de Diseño
- **Estructura Estricta (SantiagoMacias):** Se respetará exactamente la misma separación de paquetes: `adapter.in`, `adapter.out.persistence` (con sus `entity`, `repository`, `mapper`), `application.port.input`, `application.port.output`, `application.usecase`, `domain.repository`, `domain.service`.
- **Documentación SDD:** Toda la documentación de diseño (Software Design Document) se redactará en inglés dentro de la carpeta `SDD/` por cada hito completado.
- **Simulación de Pagos:** Se mantendrá a nivel de dominio una lógica que procese el pago con probabilidad de fallo, permitiendo al comprador (`BUYER`) reintentar la operación y probar el flujo de error.
- **Manejo de Base de Datos:** Las tablas se crearán automáticamente desde las Entidades JPA mediante `spring.jpa.hibernate.ddl-auto=update`.
- **Notificaciones por Correo:** Se implementará el envío de correos mediante un Adaptador de Salida (`EmailNotificationAdapter`).
- **Lógica Específica del PDF:** 
  - División entre productos Físicos (requieren despacho) y Digitales (entrega inmediata tras pago).
  - Trazabilidad estricta de inventario: Se incluye la entidad `InventoryMovement` para rastrear ingresos, reservas, salidas, ajustes y devoluciones. No se permiten existencias negativas ni reservar productos dañados.
  - Vendedores solo pueden ser registrados por el Administrador.

---

## Fases de Implementación

### Fase 1: Configuración Base, Arquitectura Inicial y SDD (Entregable 1)
_Objetivo: Estructurar el cascarón del proyecto y preparar la base de la documentación._

- [ ] 1.1. Inicializar proyecto Spring Boot (Web, Data JPA, Security, Validation, MySQL/H2, JavaMailSender).
- [ ] 1.2. Configurar `application.properties` (conexión a BD, variables JWT, dialecto de Hibernate, credenciales SMTP).
- [ ] 1.3. Crear estructura de paquetes Hexagonal (`adapter`, `application`, `domain`, `config`, `shared`).
- [ ] 1.4. Implementar utilidades compartidas y configuraciones globales:
  - [ ] 1.4.1. `GlobalExceptionHandler` y envoltorio de respuesta unificado `ApiResponse`.
  - [ ] 1.4.2. `JacksonConfig` para serialización de fechas y objetos.
- [ ] 1.5. Crear directorio `SDD/` y redactar `SDD/01_ARCHITECTURE_OVERVIEW.md` (En inglés).

### Fase 2: Modelado del Dominio - Estructura y Reglas (Entregable 2)
_Objetivo: Diseñar el núcleo del negocio sin dependencias tecnológicas, dividido en sus respectivos subpaquetes (`domain.model`, `domain.exception`, `domain.repository`)._

- [ ] 2.1. **Value Objects (`domain.model.valueobject`):**
  - [ ] 2.1.1. `Money` (Manejo seguro de moneda), `Email`, `DocumentId`.
  - [ ] 2.1.2. `StockQuantity` (Validación de enteros no negativos).
  - [ ] 2.1.3. Enums: `UserRole` (BUYER, SELLER, LOGISTICS, ADMIN, SUPERVISOR), `UserStatus`, `ProductType` (PHYSICAL, DIGITAL), `ProductStatus`, `OrderStatus`, `MovementType`.
- [ ] 2.2. **Entities y Aggregates (`domain.model.entity` y `domain.model.aggregate`):**
  - [ ] 2.2.1. `User` (Raíz) y `BuyerProfile` (Entidad).
  - [ ] 2.2.2. `Category` y `AuditLog` (Entidades).
  - [ ] 2.2.3. `Warehouse` y `InventoryMovement` (Entidades - Rastreo transaccional de inventario).
  - [ ] 2.2.4. `Product` (Agregado Raíz) e `InventoryItem` (Asociación Bodega-Producto).
  - [ ] 2.2.5. `Order` (Agregado Raíz) y `OrderItem` (Entidad).
- [ ] 2.3. **Exceptions (`domain.exception`):** `DomainValidationException`, `NegativeStockException`, `InvalidReservationException`, `PaymentRejectedException`.
- [ ] 2.4. **Domain Repositories (`domain.repository`):** (Puertos de salida a nivel de dominio) `UserRepository`, `ProductRepository`, `OrderRepository`, `WarehouseRepository`.
- [ ] 2.5. Documentar el modelo de dominio en `SDD/02_DOMAIN_LAYER.md`.

### Fase 3: Servicios y Eventos de Dominio (Entregable 3)
_Objetivo: Implementar las reglas de negocio complejas que cruzan múltiples agregados, ubicadas en `domain.service`._

- [ ] 3.1. Definir Eventos de Dominio (`DomainEvent`):
  - [ ] 3.1.1. `OrderCreatedEvent`, `OrderPaidEvent`, `OrderPaymentFailedEvent`, `StockDepletedEvent`.
- [ ] 3.2. **Domain Services (`domain.service`):**
  - [ ] 3.2.1. `PaymentSimulationDomainService` (Lógica que evalúa aleatoriamente el pago forzando reintentos).
  - [ ] 3.2.2. `InventoryDomainService` (Manejo de reservas, deducciones, trazabilidad en `InventoryMovement` y diferencia Físico vs Digital).
  - [ ] 3.2.3. `OrderCheckoutDomainService` (Lógica de transición de estados invocando simulación y reserva).

### Fase 4: Capa de Aplicación - Casos de Uso y Puertos
_Objetivo: Orquestar el dominio mediante los flujos de negocio en el paquete `application`._

- [ ] 4.1. **DTOs (`application.dto`):** Records de request/response para cada entidad.
- [ ] 4.2. **Input Ports (`application.port.input`):** `UserInputPort`, `ProductInputPort`, `OrderInputPort`, `InventoryInputPort`.
- [ ] 4.3. **Output Ports (`application.port.output`):** `UserRepositoryPort`, `ProductRepositoryPort`, `OrderRepositoryPort`, `EmailNotificationPort`.
- [ ] 4.4. **Use Cases (`application.usecase`):**
  - [ ] 4.4.1. `UserUseCase` (Registro validando roles).
  - [ ] 4.4.2. `ProductUseCase` y `InventoryUseCase`.
  - [ ] 4.4.3. `OrderUseCase` (Checkout, reintentar pago).
- [ ] 4.5. Documentar los casos de uso en `SDD/03_APPLICATION_LAYER.md`.

### Fase 5: Persistencia e Infraestructura (Adaptadores de Salida)
_Objetivo: Conectar el dominio con MySQL y envío de correos en `adapter.out.persistence`._

- [ ] 5.1. Implementar `EmailNotificationAdapter` que consuma `JavaMailSender` y cumpla con el puerto de salida.
- [ ] 5.2. **JPA Entities (`adapter.out.persistence.entity`):** `UserJpaEntity`, `ProductJpaEntity`, `OrderJpaEntity`, `InventoryMovementJpaEntity`, etc.
- [ ] 5.3. **Spring Data Repositories (`adapter.out.persistence.repository`):** `UserJpaRepository`, etc.
- [ ] 5.4. **Mappers (`adapter.out.persistence.mapper`):** Clases explícitas para convertir entre Entidades de Dominio y Entidades JPA.
- [ ] 5.5. **Persistence Adapters (`adapter.out.persistence`):** Clases que implementan los `OutputPorts` inyectando repositorios JPA.
- [ ] 5.6. Documentar persistencia en `SDD/04_PERSISTENCE_LAYER.md`.

### Fase 6: Controladores REST y Seguridad (Adaptadores de Entrada)
_Objetivo: Exponer la API hacia el exterior de forma segura en `adapter.in.web`._

- [ ] 6.1. Configuración de Spring Security en la capa `config`:
  - [ ] 6.1.1. `JwtTokenProvider` y `JwtAuthenticationFilter`.
  - [ ] 6.1.2. `SecurityConfig` aplicando la matriz de responsabilidades estricta.
- [ ] 6.2. **Controllers (`adapter.in.web.controller`):**
  - [ ] 6.2.1. `AuthController`, `UserController`, `ProductController`, `OrderController`, `InventoryController`.
- [ ] 6.3. Integrar Swagger/OpenAPI UI para documentar automáticamente los endpoints.

### Fase 7: Schedulers y Refinamientos Finales
_Objetivo: Añadir automatizaciones de negocio y pulir detalles (Ubicado en `adapter.in.scheduler`)._

- [ ] 7.1. Implementar `OrderExpirationScheduler` (Cancelar órdenes no pagadas en X minutos y restaurar stock).
- [ ] 7.2. Implementar módulo de Auditoría (Anotaciones o AOP para alimentar la entidad `AuditLog`).
- [ ] 7.3. Carga inicial de datos de prueba (`DataSeeder` para un usuario `ADMIN` por defecto que pueda registrar a los vendedores).
- [ ] 7.4. Revisión final y pulido de toda la documentación SDD en inglés.
