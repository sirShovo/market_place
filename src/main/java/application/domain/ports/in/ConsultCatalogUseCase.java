package application.domain.ports.in;

import application.domain.models.Product;
import java.util.List;

/** Lists the published catalog. */
public interface ConsultCatalogUseCase {

    List<Product> consultPublished();
}
