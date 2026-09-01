package application.domain.services.seller;

import application.domain.enums.AuditSeverity;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.DuplicateUserException;
import application.domain.models.Operation;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.in.OnboardSellerUseCase;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.SellerStatus;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.WarehouseType;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * An {@code ADMIN} onboards a seller together with its first warehouse in a single
 * flow (spec Domain 3, flow 6.1.1). Sellers cannot self-register.
 */
@Service
@RequiredArgsConstructor
public class OnboardSellerService implements OnboardSellerUseCase {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public Seller onboard(User requester, Seller seller, Warehouse firstWarehouse) {
        validateUserStatusService.execute(requester);
        validateRoleAuthorizationService.execute(requester, UserRole.ADMIN);

        if (firstWarehouse == null) {
            throw new DomainException("A seller must be onboarded with its first warehouse.");
        }
        if (sellerRepositoryPort.existsByIdentification(seller)) {
            throw new DuplicateUserException("A seller with this document already exists.");
        }

        seller.setRole(UserRole.SELLER);
        seller.setStatus(SellerStatus.ACTIVE);
        seller.setOnboardedBy(requester);
        Seller savedSeller = sellerRepositoryPort.save(seller);

        firstWarehouse.setType(WarehouseType.SELLER);
        firstWarehouse.setOwner(savedSeller);
        Warehouse savedWarehouse = warehouseRepositoryPort.save(firstWarehouse);
        savedSeller.getWarehouses().add(savedWarehouse);
        sellerRepositoryPort.update(savedSeller);

        Operation operation = Operation.of(OperationType.SELLER_ONBOARDING, requester, savedSeller);
        registerOperationAndAuditService.execute(operation, AuditSeverity.INFO, Map.of(
                "seller", savedSeller.auditableId(),
                "firstWarehouse", savedWarehouse.getIdentifier()));
        return savedSeller;
    }
}
