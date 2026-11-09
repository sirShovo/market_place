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

## Estado

- [x] **Fase 1** — pom limpio, paquetes con raíz `application`, `infrastructure/config`,
  `NexusMarketApplication`, `SDD/Software Architecture/Software Architecture.md`.
- [x] **Fase 2** — `DomainCatalog` + catálogos + enums + VOs (records) + jerarquía de
  modelos + excepciones; `SDD/domain/Domain Model.md` y `Domain Value Objects.md`.
- [x] **Fase 3** — servicios por subdominio (`user`, `buyer`, `seller`, `warehouse`,
  `catalog`, `inventory`, `cart`, `order`, `authorization`, `operation`) con
  Operation/Audit y autorización; `SDD/domain/Domain Services.md` + `services/*.md`.
- [x] **Fase 4** — `ports/out` (16) y `ports/in` (33) con firmas de modelo de dominio;
  retiro de la capa legacy `dto/usecase/port/event`; `SDD/domain/Output Ports.md` e
  `Input Ports.md`. Wiring automático por `@Service`.
- [ ] **Fase 5+** — persistencia (adapters de los puertos), simulación de pago,
  auditoría Mongo, REST + seguridad, schedulers.
- [x] **Endurecimiento y cierre de Fase 4** — revisión integral de servicios, puertos y SDD:
  1. Unicidad de documento/correo (§11) validada **entre `User`, `Buyer` y
     `Seller` a la vez** vía `ValidatePlatformUniquenessService`
     (`application.domain.services.identity`), más unicidad de `username` en los tres
     flujos de registro (`RegisterUserService`, `RegisterBuyerService`, `OnboardSellerService`).
  2. `RegisterBuyerService` y `OnboardSellerService` crean el `User`
     de login enlazado por `identification`, cerrando el vacío de autenticación.
  3. `ProcessOrderPaymentService` valida con `ValidateBuyerOwnsOrderService` que la
     orden pertenezca al comprador que paga.
  4. `CheckoutCartService` reserva inventario para cada línea física vía
     `ReserveStockForOrderItemService`; `OrderItem` ganó el campo `warehouse`.
  5. `DispatchOrderService` registra movimientos `SALE_EXIT` por cada línea física
     contra la bodega asignada en el checkout.
  6. Se incorporaron `RegisterInventoryReturnService` (`RegisterInventoryReturnUseCase`),
     `ChangeBuyerCommercialStatusService` (`ChangeBuyerCommercialStatusUseCase`),
     `ValidateSellerOwnershipService` (permitiendo a `SELLER` consultar su propio perfil
     en `ConsultSellerService`), `ConsultSellerProductsService`
     (`ConsultSellerProductsUseCase`) y `ConsultBuyerOrdersService`
     (`ConsultBuyerOrdersUseCase`).

  52 tests unitarios de dominio en verde.

Build verde en cada fase (`./mvnw clean test`); `@SpringBootTest` `@Disabled` hasta
fase 6.

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
   `@Service`, método de negocio, que compone otros servicios (autorización, auditoría).
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
