package application.domain.ports.in;

import application.domain.models.Product;
import application.domain.models.User;

/** A {@code SELLER} publishes a product to the catalog. */
public interface PublishProductUseCase {

    Product publish(User requester, Product product);
}
