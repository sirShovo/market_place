# User Services

## Introduction

This document defines the services belonging to the **User Administration** subdomain
of NexusMarket (spec **Domain 1**). These services own the identity and system-access
lifecycle of a `User`: registration, status changes and consultation.

Every operation is performed by an authenticated user (spec RG01) and every
state-changing action generates an `Operation` and an `AuditLog` record (spec
traceability requirements).

The services described here define the conceptual behavior of the subdomain. REST
contracts, persistence mappings and infrastructure concerns are documented separately
(phases 5–6).

Location: `application.domain.services.user`.

---

## Domain Model Context

```text
Person (abstract)
 ├── identification : DocumentId     (unique across the platform — spec §11)
 ├── fullName       : String         (not empty)
 ├── email          : Email          (unique across the platform — spec §11)
 ├── phoneNumber    : String
 ├── address        : String
 └── role           : UserRole       (exactly one per participant — spec RG02)

User (extends Person, implements AuditableEntity)
 ├── userId   : Integer
 ├── username : String
 ├── password : String               (hash only — never the plain value)
 └── status   : UserStatus           (ACTIVE / BLOCKED / INACTIVE)
```

`role` lives on `Person` and is never duplicated on `User`. Internal roles
(`LOGISTICS_OPERATOR`, `ADMIN`, `SUPERVISOR`) are represented by a plain `User` with no
extra profile.

---

## Service Design Principles

### Domain Model parameters

Services and Input Ports receive Domain Models. They never receive primitive
identifiers, REST request DTOs or persistence entities.

Incorrect:

```java
User register(String requesterId, String username, String role);
```

Correct:

```java
User register(User requester, User newUser);
```

### External information

Services use the data already in the Domain Models first, and reach the outside only
through Output Ports. They never touch JPA, SQL, a password library or HTTP directly.

```text
User Service
      │
      ▼
Output Port  (UserRepositoryPort / PasswordServicePort)
      │
      ▼
Output Adapter
      │
      ▼
External Resource
```

### Wiring

`@Service` + constructor injection (`@RequiredArgsConstructor`). Each service is
unit-testable by direct instantiation with test-double ports.

---

## 1. Register User

### Description

Registers a system `User`. User administration is an `ADMIN` responsibility; in
particular a `SELLER` user can only be created by an `ADMIN` (spec Domain 3). The
document identifier and e-mail must be unique across the whole platform (spec §11).

Input Port: `RegisterUserUseCase` — `User register(User requester, User newUser)`.

### Input

```text
User    (requester)
User    (newUser — carries username, password, role, identification, email)
```

### Authorization

* `ValidateUserStatusService` — requester is `ACTIVE` (spec RG01).
* `ValidateRoleAuthorizationService` — requester role is `ADMIN`.

### Domain Validations

* `UserRepositoryPort.existsByIdentification(newUser)` must be `false`, otherwise
  `DuplicateUserException`.
* `UserRepositoryPort.existsByEmail(newUser)` must be `false`, otherwise
  `DuplicateUserException`.

### Effect / State Change

* `status` is set to `ACTIVE`.
* `password` is replaced by `PasswordServicePort.encrypt(rawPassword)`.
* The user is persisted.

### Persistence

```text
UserRepositoryPort.existsByIdentification(newUser)
UserRepositoryPort.existsByEmail(newUser)
UserRepositoryPort.save(newUser)
```

### Operation and Audit

Operation type: `USER_REGISTRATION`, severity `INFO`.

```text
newUser
  │
  ▼
RegisterUserService
  ├── uniqueness checks
  ├── encrypt password
  ├── UserRepositoryPort.save
  └── Operation (USER_REGISTRATION)
         │
         ▼
       AuditLog   details = { username, role }
```

---

## 2. Change User Status

### Description

Activates, blocks or deactivates a system user. The desired status travels on the
supplied `target` model.

Input Port: `ChangeUserStatusUseCase` — `User changeStatus(User requester, User target)`.

### Input

```text
User    (requester)
User    (target — carries username and the desired status)
```

### Authorization

* `ValidateUserStatusService` — requester is `ACTIVE`.
* `ValidateRoleAuthorizationService` — requester role is `ADMIN`.

### Domain Validations

* The target user must exist (`UserRepositoryPort.findByUsername`), otherwise
  `EntityNotFoundException`.

### Effect / State Change

* The stored user's `status` is set to `target.status`.

### Persistence

```text
UserRepositoryPort.findByUsername(target)
UserRepositoryPort.update(storedUser)
```

### Operation and Audit

Operation type: `USER_STATUS_CHANGE`, severity `WARNING`, details
`{ username, previousStatus, newStatus }`.

---

## 3. Consult User

### Description

Returns a system user by username. The result never exposes persistence entities and
never carries the password hash beyond the domain boundary (the REST layer maps to a
response DTO in phase 6).

Input Port: `ConsultUserUseCase` — `User consult(User requester, User probe)`.

### Input

```text
User    (requester)
User    (probe — carries the username)
```

### Authorization

* `ValidateUserStatusService` — requester is `ACTIVE`.
* `ValidateRoleAuthorizationService` — requester role is `ADMIN` or `SUPERVISOR`.

### Domain Validations

* The user must exist (`EntityNotFoundException`).

### Operation and Audit

Not an audited operation (read-only).

---

## Output Ports

```java
interface UserRepositoryPort {
    User save(User user);
    Optional<User> findByUsername(User user);
    Optional<User> findByIdentification(User user);
    boolean existsByIdentification(User user);
    boolean existsByEmail(User user);
    void update(User user);
}

interface PasswordServicePort {
    String encrypt(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}

interface OperationRepositoryPort { Operation save(Operation operation); /* ... */ }
interface AuditLogRepositoryPort  { AuditLog  save(AuditLog auditLog);   /* ... */ }
```

See [Output Ports](../Output%20Ports.md) for the full contracts.

---

## Input Ports

```java
interface RegisterUserUseCase     { User register(User requester, User newUser); }
interface ChangeUserStatusUseCase { User changeStatus(User requester, User target); }
interface ConsultUserUseCase      { User consult(User requester, User probe); }
```

---

## Registration Flow

```text
HTTP Request  (Phase 6)
     │
     ▼
Request DTO ──► Request Mapper ──► User (requester), User (newUser)
     │
     ▼
RegisterUserUseCase
     │
     ▼
RegisterUserService
     ├── ValidateUserStatusService
     ├── ValidateRoleAuthorizationService (ADMIN)
     ├── UserRepositoryPort.existsByIdentification / existsByEmail
     ├── PasswordServicePort.encrypt
     ├── UserRepositoryPort.save
     └── RegisterOperationAndAuditService (USER_REGISTRATION)
```
