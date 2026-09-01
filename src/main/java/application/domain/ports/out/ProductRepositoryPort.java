package application.domain.ports.out;

import application.domain.models.Product;
import application.domain.models.Seller;
import java.util.List;
import java.util.Optional;

/** Persistence contract for {@link Product}. */
public interface ProductRepositoryPort {

    Product save(Product product);

    Optional<Product> findByIdentifier(Product product);

    List<Product> findBySeller(Seller seller);

    List<Product> findPublished();

    void update(Product product);
}
