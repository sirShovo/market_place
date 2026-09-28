package application.domain.services.seller;

import application.domain.enums.AuditSeverity;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.DuplicateUserException;
import application.domain.models.Operation;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.in.OnboardSellerUseCase;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.services.identity.ValidatePlatformUniquenessService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.SellerStatus;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.UserStatus;
import application.domain.valueobjects.WarehouseType;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * An {@code ADMIN} onboards a seller together with its first warehouse in a single
 * flow (spec Domain 3, flow 6.1.1). Sellers cannot self-register. A login
 * {@link User} is created alongside the {@link Seller} profile, sharing its identity,
 * so the seller can authenticate once Phase 6 exists.
 */
@Service
@RequiredArgsConstructor
public class OnboardSellerService implements OnboardSellerUseCase {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidatePlatformUniquenessService validatePlatformUniquenessService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public Seller onboard(User requester, Seller seller, Warehouse firstWarehouse, User sellerAccount) {
        validateUserStatusService.execute(requester);
        validateRoleAuthorizationService.execute(requester, UserRole.ADMIN);

        if (firstWarehouse == null) {
            throw new DomainException("A seller must be onboarded with its first warehouse.");
        }
        if (sellerAccount == null || sellerAccount.getUsername() == null || sellerAccount.getUsername().isBlank()
                || sellerAccount.getPassword() == null || sellerAccount.getPassword().isBlank()) {
            throw new DomainException("A seller must be onboarded with login credentials.");
        }
        validatePlatformUniquenessService.execute(seller.getIdentification(), seller.getEmail());
        if (userRepositoryPort.findByUsername(sellerAccount).isPresent()) {
            throw new DuplicateUserException("This username is already taken.");
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

        sellerAccount.setIdentification(seller.getIdentification());
        sellerAccount.setFullName(seller.getFullName());
        sellerAccount.setEmail(seller.getEmail());
        sellerAccount.setPhoneNumber(seller.getPhoneNumber());
        sellerAccount.setAddress(seller.getAddress());
        sellerAccount.setRole(UserRole.SELLER);
        sellerAccount.setStatus(UserStatus.ACTIVE);
        sellerAccount.setPassword(passwordServicePort.encrypt(sellerAccount.getPassword()));
        userRepositoryPort.save(sellerAccount);

        Operation operation = Operation.of(OperationType.SELLER_ONBOARDING, requester, savedSeller);
        registerOperationAndAuditService.execute(operation, AuditSeverity.INFO, Map.of(
                "seller", savedSeller.auditableId(),
                "firstWarehouse", savedWarehouse.getIdentifier(),
                "username", sellerAccount.getUsername()));
        return savedSeller;
    }
}
