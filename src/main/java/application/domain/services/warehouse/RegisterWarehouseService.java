package application.domain.services.warehouse;

import application.domain.enums.AuditSeverity;
import application.domain.exceptions.DomainException;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.in.RegisterWarehouseUseCase;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.WarehouseType;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * An {@code ADMIN} registers an additional warehouse (spec Domain 4). A
 * {@code SELLER}-type warehouse must reference its owning seller; a
 * {@code MARKETPLACE}-type warehouse must not.
 */
@Service
@RequiredArgsConstructor
public class RegisterWarehouseService implements RegisterWarehouseUseCase {

    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public Warehouse register(User requester, Warehouse warehouse) {
        validateUserStatusService.execute(requester);
        validateRoleAuthorizationService.execute(requester, UserRole.ADMIN);

        boolean sellerOwned = WarehouseType.SELLER.equals(warehouse.getType());
        if (sellerOwned && warehouse.getOwner() == null) {
            throw new DomainException("A seller warehouse requires an owner.");
        }
        if (!sellerOwned && warehouse.getOwner() != null) {
            throw new DomainException("A marketplace warehouse must not have an owner.");
        }

        Warehouse saved = warehouseRepositoryPort.save(warehouse);

        Operation operation = Operation.of(OperationType.WAREHOUSE_REGISTRATION, requester, saved);
        registerOperationAndAuditService.execute(operation, AuditSeverity.INFO, Map.of(
                "warehouse", saved.getIdentifier(),
                "type", saved.getType().getCode()));
        return saved;
    }
}
