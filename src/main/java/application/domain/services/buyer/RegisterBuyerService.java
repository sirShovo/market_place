package application.domain.services.buyer;

import application.domain.enums.AuditSeverity;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.DuplicateUserException;
import application.domain.models.Buyer;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.RegisterBuyerUseCase;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.identity.ValidatePlatformUniquenessService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.BuyerCommercialStatus;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.UserStatus;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Self-service buyer registration (spec Domain 2). Document and e-mail are unique
 * across the whole platform, not just among buyers (spec §11); the main delivery
 * address is mandatory. A login {@link User} is created alongside the {@link Buyer}
 * profile, sharing its identity, so the buyer can authenticate once Phase 6 exists.
 */
@Service
@RequiredArgsConstructor
public class RegisterBuyerService implements RegisterBuyerUseCase {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final ValidatePlatformUniquenessService validatePlatformUniquenessService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public Buyer register(Buyer buyer, User account) {
        if (buyer.getMainAddress() == null || buyer.getMainAddress().isBlank()) {
            throw new DomainException("A buyer requires a main delivery address.");
        }
        if (account == null || account.getUsername() == null || account.getUsername().isBlank()
                || account.getPassword() == null || account.getPassword().isBlank()) {
            throw new DomainException("A buyer requires login credentials.");
        }
        validatePlatformUniquenessService.execute(buyer.getIdentification(), buyer.getEmail());
        if (userRepositoryPort.findByUsername(account).isPresent()) {
            throw new DuplicateUserException("This username is already taken.");
        }

        buyer.setRole(UserRole.BUYER);
        buyer.setCommercialStatus(BuyerCommercialStatus.ACTIVE);
        Buyer savedBuyer = buyerRepositoryPort.save(buyer);

        account.setIdentification(buyer.getIdentification());
        account.setFullName(buyer.getFullName());
        account.setEmail(buyer.getEmail());
        account.setPhoneNumber(buyer.getPhoneNumber());
        account.setAddress(buyer.getAddress());
        account.setRole(UserRole.BUYER);
        account.setStatus(UserStatus.ACTIVE);
        account.setPassword(passwordServicePort.encrypt(account.getPassword()));
        userRepositoryPort.save(account);

        Operation operation = Operation.of(OperationType.USER_REGISTRATION, null, savedBuyer);
        registerOperationAndAuditService.execute(operation, AuditSeverity.INFO, Map.of(
                "buyer", savedBuyer.auditableId(),
                "username", account.getUsername()));
        return savedBuyer;
    }
}
