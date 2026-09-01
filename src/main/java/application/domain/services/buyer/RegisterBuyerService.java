package application.domain.services.buyer;

import application.domain.enums.AuditSeverity;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.DuplicateUserException;
import application.domain.models.Buyer;
import application.domain.models.Operation;
import application.domain.ports.in.RegisterBuyerUseCase;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.BuyerCommercialStatus;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.UserRole;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Self-service buyer registration (spec Domain 2). Document and e-mail are unique
 * across the platform (spec §11); the main delivery address is mandatory.
 */
@Service
@RequiredArgsConstructor
public class RegisterBuyerService implements RegisterBuyerUseCase {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public Buyer register(Buyer buyer) {
        if (buyer.getMainAddress() == null || buyer.getMainAddress().isBlank()) {
            throw new DomainException("A buyer requires a main delivery address.");
        }
        if (buyerRepositoryPort.existsByIdentification(buyer)) {
            throw new DuplicateUserException("A buyer with this document already exists.");
        }
        if (buyerRepositoryPort.existsByEmail(buyer)) {
            throw new DuplicateUserException("A buyer with this e-mail already exists.");
        }

        buyer.setRole(UserRole.BUYER);
        buyer.setCommercialStatus(BuyerCommercialStatus.ACTIVE);
        Buyer saved = buyerRepositoryPort.save(buyer);

        Operation operation = Operation.of(OperationType.USER_REGISTRATION, null, saved);
        registerOperationAndAuditService.execute(operation, AuditSeverity.INFO, Map.of(
                "buyer", saved.auditableId()));
        return saved;
    }
}
