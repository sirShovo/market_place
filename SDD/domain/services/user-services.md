# User Services

Subdomain: `application.domain.services.user`. Covers spec **Domain 1 — User
Administration**.

Every service validates the requester through `authorization/` and (for commands)
records an `Operation` + `AuditLog` via `RegisterOperationAndAuditService`.

---

## RegisterUserService — `RegisterUserUseCase`

Registers a system `User`.

* **Authorization:** requester `ACTIVE` (RG01) and role `ADMIN`. In particular a
  `SELLER` user is only created by an `ADMIN` (spec Domain 3).
* **Validations:** document and e-mail unique across the platform (spec §11) via
  `UserRepositoryPort.existsByIdentification` / `existsByEmail`; otherwise
  `DuplicateUserException`.
* **Effect:** status set to `ACTIVE`; password encrypted through
  `PasswordServicePort`; persisted.
* **Audit:** `USER_REGISTRATION`, severity `INFO`.
* **Ports:** `UserRepositoryPort`, `PasswordServicePort`.

## ChangeUserStatusService — `ChangeUserStatusUseCase`

Activates / blocks / deactivates a user; the target status travels on the supplied
model.

* **Authorization:** requester `ACTIVE` and role `ADMIN`.
* **Validations:** target user must exist (`EntityNotFoundException`).
* **Audit:** `USER_STATUS_CHANGE`, severity `WARNING`, details record previous/new
  status.
* **Ports:** `UserRepositoryPort`.

## ConsultUserService — `ConsultUserUseCase`

Returns a user by username.

* **Authorization:** requester `ACTIVE` and role `ADMIN` or `SUPERVISOR`.
* **Not audited.**
* **Ports:** `UserRepositoryPort`.
