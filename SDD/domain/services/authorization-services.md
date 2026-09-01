# Authorization Services

Subdomain: `application.domain.services.authorization`. These are **internal
collaborators** composed by the use-case services; they are not exposed as Input Ports
and have no REST entry point. They enforce spec RG01, RG03 and the responsibility
matrix §12.

Each throws `UnauthorizedOperationException` on failure.

---

## ValidateRoleAuthorizationService

`execute(User requester, UserRole... allowedRoles)` — passes only if the requester has
a role and it is one of `allowedRoles`.

## ValidateUserStatusService

`execute(User requester)` — passes only if the requester is non-null and
`status == ACTIVE` (spec RG01).

## ValidateBuyerOwnershipService

`execute(User requester, Buyer target)` — `ADMIN` / `SUPERVISOR` may access any buyer;
a `BUYER` only if its `identification` matches the target's (spec RG03).

## ValidateProductOwnershipService

`execute(User requester, Product product)` — `ADMIN` may manage any product; a
`SELLER` only its own (product's seller `identification` matches the requester's).

## ValidateOrderAccessService

`execute(User requester, Order order)` — `ADMIN` / `SUPERVISOR` /
`LOGISTICS_OPERATOR` may access any order; a `BUYER` only its own.

## ValidateBuyerCanPurchaseService

`execute(Buyer buyer)` — passes only if the buyer's `commercialStatus == ACTIVE`
(spec Domain 2). Used by cart and order services.
