package application.domain.services.catalog;

import application.domain.enums.AuditSeverity;
import application.domain.exceptions.DomainException;
import application.domain.models.Operation;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.in.PublishProductUseCase;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.UserRole;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * A {@code SELLER} publishes a product to the catalog (spec Domain 5, responsibility
 * matrix §12). The product is created {@code PUBLISHED}.
 */
@Service
@RequiredArgsConstructor
public class PublishProductService implements PublishProductUseCase {

    private final ProductRepositoryPort productRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public Product publish(User requester, Product product) {
        validateUserStatusService.execute(requester);
        validateRoleAuthorizationService.execute(requester, UserRole.SELLER);

        if (product.getSeller() == null) {
            throw new DomainException("A product must reference its seller.");
        }
        if (product.getPrice() == null) {
            throw new DomainException("A product requires a price.");
        }

        product.setStatus(ProductStatus.PUBLISHED);
        Product saved = productRepositoryPort.save(product);

        Operation operation = Operation.of(OperationType.PRODUCT_PUBLICATION, requester, saved);
        registerOperationAndAuditService.execute(operation, AuditSeverity.INFO, Map.of(
                "product", saved.getIdentifier(),
                "type", saved.getType().getCode()));
        return saved;
    }
}
