package application.domain.services.catalog;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.in.UpdateProductUseCase;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.services.authorization.ValidateProductOwnershipService;
import application.domain.services.authorization.ValidateUserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * A seller updates its own product's descriptive data, price, category and variants.
 * Status changes go through {@link ChangeProductStatusService}.
 */
@Service
@RequiredArgsConstructor
public class UpdateProductService implements UpdateProductUseCase {

    private final ProductRepositoryPort productRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateProductOwnershipService validateProductOwnershipService;

    @Override
    public Product update(User requester, Product product) {
        validateUserStatusService.execute(requester);

        Product stored = productRepositoryPort.findByIdentifier(product)
                .orElseThrow(() -> new EntityNotFoundException("Product"));
        validateProductOwnershipService.execute(requester, stored);

        stored.setName(product.getName());
        stored.setDescription(product.getDescription());
        stored.setPrice(product.getPrice());
        stored.setCategory(product.getCategory());
        stored.setVariants(product.getVariants());
        productRepositoryPort.update(stored);
        return stored;
    }
}
