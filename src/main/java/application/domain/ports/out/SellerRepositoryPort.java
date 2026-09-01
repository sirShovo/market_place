package application.domain.ports.out;

import application.domain.models.Seller;
import java.util.List;
import java.util.Optional;

/** Persistence contract for {@link Seller}. */
public interface SellerRepositoryPort {

    Seller save(Seller seller);

    Optional<Seller> findByIdentification(Seller seller);

    boolean existsByIdentification(Seller seller);

    List<Seller> findAll();

    void update(Seller seller);
}
