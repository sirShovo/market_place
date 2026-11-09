# REST and Security Adapters

## Overview

Phase 6 completes the inbound adapter layer (`application.adapters.in.rest`) and the security infrastructure (`application.infrastructure.security`), exposing the 34 domain use cases (including authentication) over HTTP/JSON with stateless JWT security and OpenAPI/Swagger documentation.

```text
HTTP Request (JSON + Bearer JWT)
      │
      ▼
JwtAuthenticationFilter     (application.infrastructure.security)
      │  validates JWT via JjwtAdapter & resolves domain User in SecurityContext
      ▼
REST Controller             (application.adapters.in.rest.controllers)
      │  maps Request DTO -> Domain Model via *RestMapper
      ▼
Input Port                  (application.domain.ports.in.*UseCase)
      │
      ▼
Domain Service              (application.domain.services.**)
```

---

## 1. Security Infrastructure & Authentication (`application.infrastructure.security` & `domain`)

### 1.1 Security Output Adapters

| Output Port | Adapter Implementation | Responsibility |
|---|---|---|
| `PasswordServicePort` | `BCryptPasswordAdapter` | Hashes and verifies passwords using Spring Security's `BCryptPasswordEncoder`. |
| `JwtServicePort` | `JjwtAdapter` | Generates signed HMAC-SHA256 JWT tokens carrying `sub` (`username`), `role`, and `identification` claims (never the password), and parses/validates tokens for inbound requests. |

### 1.2 Authentication Use Case (`LoginUseCase` & `LoginService`)

- **Input Port:** `LoginUseCase` (`application.domain.ports.in.LoginUseCase`) exposes `String login(User credentials)` (and convenience `login(String username, String rawPassword)`).
- **Domain Service:** `LoginService` (`application.domain.services.user.LoginService`):
  1. Validates non-blank username and password; throws `InvalidCredentialsException` otherwise.
  2. Looks up the user via `UserRepositoryPort.findByUsername`.
  3. Verifies the user is `UserStatus.ACTIVE` (spec RG01).
  4. Verifies the password via `PasswordServicePort.matches`.
  5. Generates and returns a signed JWT via `JwtServicePort.generateToken`.

### 1.3 Stateless JWT Filter & `SecurityConfig`

- **`JwtAuthenticationFilter`**:
  - Extracts `Authorization: Bearer <token>`.
  - Validates the token with `JjwtAdapter` and loads the domain `User` from `UserRepositoryPort` (falling back to token claims if needed).
  - Populates `SecurityContextHolder` with the domain `User` as the authentication principal and `ROLE_<role>` as the granted authority.
- **`SecurityConfig`**:
  - Configures stateless session management (`SessionCreationPolicy.STATELESS`) and disables CSRF for the REST API.
  - Public endpoints:
    - `POST /api/auth/login`
    - `POST /api/buyers/register` (buyer self-registration)
    - `GET /api/products/catalog` (public product catalog)
    - `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`
  - All other `/api/**` endpoints require authentication; fine-grained role, status, and ownership checks are enforced both at the HTTP layer and inside the domain services.
- **`AuthenticatedUserResolver`**:
  - Resolves the current domain `User` from `SecurityContextHolder` and bridges to the `Buyer` profile via `BuyerRepositoryPort.findByIdentification` using the shared `DocumentId`.

---

## 2. REST Layer (`application.adapters.in.rest`)

### 2.1 Package Structure

```text
application/adapters/in/rest/
 ├── controllers/    10 @RestController classes returning ApiResponse<T>
 ├── requests/       Jakarta Validation-annotated request records/classes
 ├── responses/      Immutable response records
 └── mappers/        Explicit REST <-> Domain mappers (*RestMapper)
```

### 2.2 Controllers & Endpoints

| Controller | Base Path | Endpoints |
|---|---|---|
| `AuthController` | `/api/auth` | `POST /login` |
| `UserController` | `/api/users` | `POST /`, `PATCH /{username}/status`, `GET /{username}` |
| `BuyerController` | `/api/buyers` | `POST /register`, `PUT /{identification}`, `PATCH /{identification}/status`, `GET /{identification}` |
| `SellerController` | `/api/sellers` | `POST /onboard`, `GET /{identification}`, `GET /{identification}/products` |
| `WarehouseController` | `/api/warehouses` | `POST /`, `GET /{identifier}` |
| `ProductController` | `/api/products` | `GET /catalog`, `POST /`, `PUT /{identifier}`, `PATCH /{identifier}/status` |
| `InventoryController` | `/api/inventory` | `POST /entries`, `POST /reservations`, `POST /releases`, `POST /adjustments`, `POST /returns`, `GET /product/{productIdentifier}` |
| `CartController` | `/api/cart` | `GET /`, `POST /items`, `DELETE /items`, `DELETE /` |
| `OrderController` | `/api/orders` | `POST /checkout`, `POST /{identifier}/pay`, `POST /{identifier}/dispatch`, `POST /{identifier}/deliver`, `GET /{identifier}`, `GET /buyer/{buyerIdentification}` |
| `AuditController` | `/api/audit` | `GET /{entityType}/{entityId}` |

### 2.3 Centralized Exception Mapping (`GlobalExceptionHandler`)

| Exception | HTTP Status |
|---|---|
| `InvalidCredentialsException` | `401 UNAUTHORIZED` |
| `UnauthorizedOperationException` | `403 FORBIDDEN` |
| `EntityNotFoundException` | `404 NOT_FOUND` |
| `DuplicateUserException` | `409 CONFLICT` |
| `DomainException` (and business rule subtypes: `NegativeStockException`, `InvalidReservationException`, `InvalidStatusTransitionException`, `FinalizedOrderModificationException`, `PaymentRejectedException`) | `400 BAD_REQUEST` |
| `MethodArgumentNotValidException` | `400 BAD_REQUEST` |
