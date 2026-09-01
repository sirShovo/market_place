package application.domain.services.catalog;

import application.domain.enums.AuditSeverity;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Operation;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.in.ChangeProductStatusUseCase;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.services.authorization.ValidateProductOwnershipService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Moves a product between PUBLISHED / SUSPENDED / DISCONTINUED (spec Domain 5). The
 * desired status is carried on the supplied {@code product} model.
 */
@Service
@RequiredArgsConstructor
public class ChangeProductStatusService implements ChangeProductStatusUseCase {

    private final ProductRepositoryPort productRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateProductOwnershipService validateProductOwnershipService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public Product changeStatus(User requester, Product product) {
        validateUserStatusService.execute(requester);

        Product stored = productRepositoryPort.findByIdentifier(product)
                .orElseThrow(() -> new EntityNotFoundException("Product"));
        validateProductOwnershipService.execute(requester, stored);

        Map<String, Object> details = new HashMap<>();
        details.put("product", stored.getIdentifier());
        details.put("previousStatus", stored.getStatus() != null ? stored.getStatus().getCode() : null);
        details.put("newStatus", product.getStatus() != null ? product.getStatus().getCode() : null);

        stored.setStatus(product.getStatus());
        productRepositoryPort.update(stored);

        Operation operation = Operation.of(OperationType.PRODUCT_STATUS_CHANGE, requester, stored);
        registerOperationAndAuditService.execute(operation, AuditSeverity.INFO, details);
        return stored;
    }
}
