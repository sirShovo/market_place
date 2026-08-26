# Plan de Implementación: API Market Place (Hexagonal + DDD)

Este documento detalla el plan de acción exhaustivo para construir la API del Market Place desde cero, basándose en la Especificación Funcional proporcionada y aplicando Arquitectura Hexagonal y Domain-Driven Design (DDD).

## Acuerdos y Lineamientos
- **Documentación SDD:** Toda la documentación de diseño (Software Design Document) se redactará y almacenará progresivamente en la carpeta `SDD/` del proyecto, completamente en inglés.
- **Roles y Restricciones:** Se reflejan fielmente los roles (Comprador, Vendedor, Operador Logístico, Administrador, Supervisor) y la matriz de responsabilidades (ej. los vendedores no pueden auto-registrarse).
- **Flujos de Producto:** Se distingue el flujo de pedidos entre productos Físicos (que pasan por logística y despacho) y productos Digitales (que pasan a entrega inmediata tras el pago).
- **Trazabilidad de Inventario:** Para asegurar que "no se permiten existencias negativas" y rastrear "Ingreso, Reserva, Salida por venta, Ajuste y Devolución", se incluye una entidad `InventoryMovement` que actúa como registro inmutable (Audit Trail).

---

## Fases de Implementación

### Fase 1: Estructura Base, Configuración y SDD Inicial
*Objetivo: Inicializar el proyecto limpio, definir la arquitectura de carpetas y crear los cimientos de la documentación SDD.*

- [ ] 1.1. Inicializar proyecto Spring Boot (Web, JPA, Security, Validation, MySQL).
- [ ] 1.2. Configurar `application.properties` (Conexión a base de datos, JWT, ddl-auto=update).
- [ ] 1.3. Crear el andamiaje Hexagonal (`adapter`, `application`, `domain`, `config`, `shared`).
- [ ] 1.4. Crear directorio raíz `SDD/` (Software Design Document).
- [ ] 1.5. Redactar `SDD/01_ARCHITECTURE_OVERVIEW.md` (En inglés): Explicación de la arquitectura Hexagonal y convenciones de código.

### Fase 2: Dominio 1, 2 y 3 - Gestión de Usuarios, Compradores y Vendedores
*Objetivo: Modelar el núcleo de autenticación y los perfiles de los participantes.*

- [ ] 2.1. Definir Value Objects: `Email`, `DocumentId` (Único), `Role` (BUYER, SELLER, LOGISTICS_OPERATOR, ADMIN, SUPERVISOR), `UserStatus`.
- [ ] 2.2. Construir Entidades: 
  - [ ] 2.2.1. `User` (Raíz).
  - [ ] 2.2.2. `BuyerProfile` (Contiene dirección principal y direcciones adicionales).
- [ ] 2.3. Implementar reglas de negocio:
  - [ ] 2.3.1. Validar unicidad de correo y documento de identidad (vía Domain Services).
  - [ ] 2.3.2. Restricción: Vendedores solo pueden ser registrados por el Administrador.
- [ ] 2.4. Documentar el modelo de usuarios en `SDD/02_USER_DOMAIN.md`.

### Fase 3: Dominio 4, 5 y 6 - Bodegas, Catálogo e Inventario
*Objetivo: Estructurar la lógica de productos físicos/digitales y el control estricto de existencias.*

- [ ] 3.1. Definir Value Objects: `ProductType` (PHYSICAL, DIGITAL), `ProductStatus` (PUBLISHED, SUSPENDED, DISCONTINUED), `StockQuantity` (No negativos), `MovementType`.
- [ ] 3.2. Construir Entidades y Agregados:
  - [ ] 3.2.1. `Warehouse` (Bodegas del marketplace y de vendedores).
  - [ ] 3.2.2. `Product` (Agregado Raíz) con soporte para variantes (Talla, Color, Modelo).
  - [ ] 3.2.3. `InventoryItem` (Asociación Producto-Bodega).
  - [ ] 3.2.4. `InventoryMovement` (Registro inmutable para trazabilidad de ajustes, reservas y ventas).
- [ ] 3.3. Implementar reglas de negocio críticas:
  - [ ] 3.3.1. Excepción: `NegativeStockException` (Bloquear existencias negativas).
  - [ ] 3.3.2. Excepción: `InvalidReservationException` (No reservar stock dañado o inexistente).
- [ ] 3.4. Documentar el modelo de catálogo e inventario en `SDD/03_CATALOG_INVENTORY_DOMAIN.md`.

### Fase 4: Dominio 7 - Gestión de Pedidos (Core Flow)
*Objetivo: Modelar el ciclo de vida central de las compras.*

- [ ] 4.1. Definir Value Objects: `OrderStatus` (CART, PENDING_PAYMENT, PAID, SHIPPED, DELIVERED/FINALIZED).
- [ ] 4.2. Construir Entidades y Agregados:
  - [ ] 4.2.1. `Order` (Agregado Raíz) y `OrderItem`.
- [ ] 4.3. Implementar lógica de transición de estados y servicios de dominio:
  - [ ] 4.3.1. Flujo Físico: Pago -> Reserva de Inventario -> Despacho -> Entrega.
  - [ ] 4.3.2. Flujo Digital: Pago -> Entrega inmediata (omite logística).
  - [ ] 4.3.3. Restricción: Inmutabilidad de pedidos finalizados.
- [ ] 4.4. Documentar el modelo de pedidos en `SDD/04_ORDER_DOMAIN.md`.

### Fase 5: Capa de Aplicación (Casos de Uso y Puertos)
*Objetivo: Conectar los flujos de negocio mediante servicios de orquestación y DTOs.*

- [ ] 5.1. Definir Input Ports y Casos de Uso (`UserUseCase`, `CatalogUseCase`, `InventoryUseCase`, `OrderUseCase`).
- [ ] 5.2. Definir Output Ports (`Repositories` y `NotificationPorts`).
- [ ] 5.3. Implementar DTOs para Request/Response.
- [ ] 5.4. Documentar los flujos de Casos de Uso en `SDD/05_APPLICATION_USE_CASES.md`.

### Fase 6: Adaptadores de Infraestructura (Persistencia JPA)
*Objetivo: Implementar los puertos de salida hacia la base de datos MySQL.*

- [ ] 6.1. Crear Entidades JPA (`UserJpaEntity`, `ProductJpaEntity`, `OrderJpaEntity`, `InventoryJpaEntity`).
- [ ] 6.2. Desarrollar Repositorios Spring Data JPA.
- [ ] 6.3. Implementar Mappers de conversión (Domain <-> JPA).
- [ ] 6.4. Implementar las clases Adapter que cumplen con las interfaces de Output Ports.

### Fase 7: Seguridad y Adaptadores Web (REST Controllers)
*Objetivo: Exponer la funcionalidad de forma segura al exterior.*

- [ ] 7.1. Configurar JWT y Spring Security (`SecurityConfig`, `JwtFilter`).
- [ ] 7.2. Implementar Controladores REST:
  - [ ] 7.2.1. `/api/auth` (Registro y Login).
  - [ ] 7.2.2. `/api/users`, `/api/products`, `/api/inventory`, `/api/orders`.
- [ ] 7.3. Implementar validaciones de seguridad basadas en la "Matriz de Responsabilidades" (ej. Solo Operador Logístico o Vendedor administran inventario; Comprador solo ve sus propios pedidos).
- [ ] 7.4. Configurar manejo global de excepciones (`GlobalExceptionHandler`).

### Fase 8: Refinamientos, Facturación y SDD Final
*Objetivo: Ajustes finales, requerimientos adicionales y consolidación documental.*

- [ ] 8.1. Implementar Módulo de Devoluciones y Reembolsos básicos.
- [ ] 8.2. Generar documentación de la API Swagger/OpenAPI.
- [ ] 8.3. Finalizar y revisar todos los documentos en el directorio `SDD/` asegurando alta calidad técnica en inglés.
