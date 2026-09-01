# Operation & Audit Services

Subdomain: `application.domain.services.operation`. Implements the cross-cutting
traceability requirement: every significant business action produces an `Operation`
and an immutable `AuditLog` record.

---

## RegisterOperationService

`execute(Operation operation)` — stamps `executionDate` when absent, persists through
`OperationRepositoryPort`.

## RegisterAuditLogService

`execute(AuditLog auditLog)` — stamps `operationDate` when absent, persists through
`AuditLogRepositoryPort` (MongoDB implementation expected; the domain is unaware).

## RegisterOperationAndAuditService

`execute(Operation operation, AuditSeverity severity, Map<String,Object> details)` —
composes the two above: persists the operation, then builds and persists a matching
`AuditLog` copying `operationType`, `performedBy`, the performer's `role`,
`affectedEntity`, plus the given `severity` and `details`.

Every command service in the other subdomains depends on this collaborator.

## ConsultAuditLogService — `ConsultAuditLogUseCase`

`consult(User requester, AuditableEntity entity)` — returns audit history for an
entity. Requester must be `ACTIVE` and hold role `ADMIN` or `SUPERVISOR`.

---

**Ports:** `OperationRepositoryPort`, `AuditLogRepositoryPort`.
