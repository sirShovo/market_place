package application.domain.services.inventory;

import application.domain.models.InventoryItem;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.in.ConsultInventoryUseCase;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.valueobjects.UserRole;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Returns the stock of a product across warehouses. Buyers never see inventory
 * (spec Domain 2, RG03).
 */
@Service
@RequiredArgsConstructor
public class ConsultInventoryService implements ConsultInventoryUseCase {

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;

    @Override
    public List<InventoryItem> consult(User requester, Product product) {
        validateUserStatusService.execute(requester);
        validateRoleAuthorizationService.execute(requester,
                UserRole.SELLER, UserRole.LOGISTICS_OPERATOR, UserRole.ADMIN, UserRole.SUPERVISOR);
        return inventoryRepositoryPort.findByProduct(product);
    }
}
