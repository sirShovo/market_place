package application.domain.ports.in;

import application.domain.models.Product;
import application.domain.models.User;

/** A seller updates its own product. */
public interface UpdateProductUseCase {

    Product update(User requester, Product product);
}
