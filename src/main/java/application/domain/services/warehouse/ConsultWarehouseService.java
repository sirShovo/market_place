package application.domain.services.warehouse;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.in.ConsultWarehouseUseCase;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.valueobjects.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Returns a warehouse. Restricted to {@code ADMIN} / {@code SUPERVISOR} /
 * {@code LOGISTICS_OPERATOR}.
 */
@Service
@RequiredArgsConstructor
public class ConsultWarehouseService implements ConsultWarehouseUseCase {

    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;

    @Override
    public Warehouse consult(User requester, Warehouse probe) {
        validateUserStatusService.execute(requester);
        validateRoleAuthorizationService.execute(requester,
                UserRole.ADMIN, UserRole.SUPERVISOR, UserRole.LOGISTICS_OPERATOR);
        return warehouseRepositoryPort.findByIdentifier(probe)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse"));
    }
}
