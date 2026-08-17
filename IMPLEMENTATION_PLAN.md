# Plan de Implementación: API Market Place (Hexagonal + DDD)

Este documento detalla el plan de acción para construir la API del Market Place en Spring Boot. Está estructurado en fases lógicas que permiten tener entregables funcionales y evaluables en cada etapa.

## Decisiones y Acuerdos de Diseño

- **Simulación de Pagos:** No se integrará una pasarela real. Se construirá una simulación a nivel de dominio (Dominio/Servicios) que procese el pago con cierta probabilidad de fallo. Esto permitirá modelar el escenario donde un pago es rechazado y el comprador (`BUYER`) debe reintentar la operación sobre la misma orden.
- **Base de Datos y Tablas:** Se utilizará un motor de base de datos MySQL local. Tal como se espera, **las tablas se crearán automáticamente** basadas en nuestras Entidades JPA. Esto se logra mediante la propiedad `spring.jpa.hibernate.ddl-auto=update` (o `create`) que instruye a Hibernate (el proveedor ORM de Spring Data) a traducir nuestras clases Java a esquema relacional.
- **Notificaciones por Correo:** Se implementará el envío de correos electrónicos. Esto formará parte de los Adaptadores de Salida (Driven Adapters) que reaccionarán a los Eventos de Dominio (ej. envío de recibo al confirmar un pedido). Se requerirá configurar una dependencia como `spring-boot-starter-mail` (JavaMailSender).

---

## Fases de Implementación

### Fase 1: Configuración Base y Arquitectura Inicial (Entregable 1)
*Objetivo: Estructurar el cascarón del proyecto y configurar las dependencias.*

- [ ] 1.1. Inicializar proyecto Spring Boot con dependencias base (Web, Data JPA, Security, Validation, MySQL/H2, JavaMailSender).
- [ ] 1.2. Configurar `application.properties` (conexión a BD, variables JWT, dialecto de Hibernate, credenciales SMTP para correos).
- [ ] 1.3. Crear estructura de paquetes Hexagonal (`adapter`, `application`, `domain`, `config`, `shared`).
- [ ] 1.4. Implementar utilidades compartidas y configuraciones globales:
  - [ ] 1.4.1. `GlobalExceptionHandler` y envoltorio de respuesta unificado `ApiResponse`.
  - [ ] 1.4.2. `JacksonConfig` para serialización de fechas y objetos.

### Fase 2: Modelado del Dominio (Entregable 2 - Específico para el Profesor)
*Objetivo: Diseñar el núcleo del negocio sin dependencias tecnológicas (Puro Java).*

- [ ] 2.1. Definir Value Objects (Objetos Inmutables):
  - [ ] 2.1.1. `Money` (Manejo seguro de moneda y montos).
  - [ ] 2.1.2. `Email`, `PhoneNumber`.
  - [ ] 2.1.3. `StockQuantity` (Validación de enteros no negativos).
  - [ ] 2.1.4. Enums de estado: `UserRole`, `UserStatus`, `OrderStatus`, `ProductStatus`.
- [ ] 2.2. Construir Entidades y Agregados:
  - [ ] 2.2.1. `User` (Entidad).
  - [ ] 2.2.2. `Category` (Entidad).
  - [ ] 2.2.3. `Product` (Agregado Raíz).
  - [ ] 2.2.4. `OrderItem` (Entidad).
  - [ ] 2.2.5. `Order` (Agregado Raíz).
  - [ ] 2.2.6. `Review` (Entidad).
  - [ ] 2.2.7. `AuditLog` (Entidad).
- [ ] 2.3. Definir Excepciones de Dominio (`DomainValidationException`, `InsufficientStockException`, `PaymentRejectedException`).
- [ ] 2.4. Definir interfaces de Repositorios (Puertos de salida a nivel de dominio):
  - [ ] 2.4.1. `UserRepository`, `ProductRepository`, `OrderRepository`, `CategoryRepository`.

### Fase 3: Servicios y Eventos de Dominio (Entregable 3 - Específico para el Profesor)
*Objetivo: Implementar las reglas de negocio complejas que cruzan múltiples agregados.*

- [ ] 3.1. Definir Eventos de Dominio (`DomainEvent`):
  - [ ] 3.1.1. `OrderCreatedEvent`, `OrderPaidEvent`, `OrderPaymentFailedEvent`.
  - [ ] 3.1.2. `StockDepletedEvent`.
- [ ] 3.2. Implementar Servicios de Dominio:
  - [ ] 3.2.1. `PaymentSimulationDomainService` (Lógica que evalúa aleatoriamente si el pago pasa o se rechaza, forzando reintentos).
  - [ ] 3.2.2. `ProductStockDomainService` (Manejo de reservas, deducciones y reintegros de inventario concurrente).
  - [ ] 3.2.3. `OrderCheckoutDomainService` (Lógica de transición de estados de la orden invocando la simulación de pagos).

### Fase 4: Capa de Aplicación (Casos de Uso y Puertos)
*Objetivo: Orquestar el dominio mediante los flujos de negocio.*

- [ ] 4.1. Definir DTOs (Records de request/response para User, Product, Order).
- [ ] 4.2. Definir Puertos de Entrada (`InputPorts`) y Salida (`OutputPorts`, incluyendo `EmailNotificationPort`).
- [ ] 4.3. Implementar Casos de Uso (Servicios de Aplicación):
  - [ ] 4.3.1. `UserUseCase` (Registro, perfil).
  - [ ] 4.3.2. `ProductUseCase` (Crear producto, actualizar, listar).
  - [ ] 4.3.3. `OrderUseCase` (Checkout, reintentar pago, cambiar estado).
  - [ ] 4.3.4. `CategoryUseCase` y `AuditLogUseCase`.

### Fase 5: Persistencia e Infraestructura (Adaptadores de Salida)
*Objetivo: Conectar el dominio con la base de datos y sistemas externos.*

- [ ] 5.1. Implementar Adaptador de Correo: `EmailNotificationAdapter` que consuma `JavaMailSender` implementando `EmailNotificationPort`.
- [ ] 5.2. Crear Entidades JPA (`UserJpaEntity`, `ProductJpaEntity`, `OrderJpaEntity`, etc.).
- [ ] 5.3. Crear interfaces Spring Data JPA (`UserRepositoryJpa`, etc.).
- [ ] 5.4. Implementar Mappers para convertir entre Entidades de Dominio y Entidades JPA.
- [ ] 5.5. Implementar Adaptadores de Persistencia que cumplan con los `OutputPorts` de repositorio.

### Fase 6: Controladores REST y Seguridad (Adaptadores de Entrada)
*Objetivo: Exponer la API hacia el exterior y protegerla.*

- [ ] 6.1. Configuración de Spring Security:
  - [ ] 6.1.1. `JwtTokenProvider` y `JwtAuthenticationFilter`.
  - [ ] 6.1.2. `SecurityConfig` (Rutas públicas vs protegidas).
  - [ ] 6.1.3. `SecurityContextHelper` (RBAC basado en roles).
- [ ] 6.2. Implementar Controladores (Endpoints HTTP):
  - [ ] 6.2.1. `AuthController` (/login, /register).
  - [ ] 6.2.2. `UserController`, `ProductController`, `OrderController`, `CategoryController`.
- [ ] 6.3. Integrar Swagger/OpenAPI UI para documentar automáticamente los endpoints.

### Fase 7: Schedulers y Refinamientos Finales
*Objetivo: Añadir automatizaciones de negocio y pulir detalles.*

- [ ] 7.1. Implementar `OrderExpirationScheduler` (Cancelar órdenes no pagadas en X minutos y restaurar stock).
- [ ] 7.2. Implementar módulo de Auditoría (Anotaciones o intercepciones AOP para alimentar el `AuditLog`).
- [ ] 7.3. Carga inicial de datos de prueba (`DataSeeder` para Categorías y un `ADMIN` por defecto).
