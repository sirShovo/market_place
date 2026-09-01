package application.domain.services.catalog;

import application.domain.models.Product;
import application.domain.ports.in.ConsultCatalogUseCase;
import application.domain.ports.out.ProductRepositoryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Lists the published catalog (spec flow 6.1 step 4: published products are publicly
 * visible).
 */
@Service
@RequiredArgsConstructor
public class ConsultCatalogService implements ConsultCatalogUseCase {

    private final ProductRepositoryPort productRepositoryPort;

    @Override
    public List<Product> consultPublished() {
        return productRepositoryPort.findPublished();
    }
}
