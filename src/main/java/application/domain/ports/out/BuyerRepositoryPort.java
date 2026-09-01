package application.domain.ports.out;

import application.domain.models.Buyer;
import java.util.Optional;

/** Persistence contract for {@link Buyer}. */
public interface BuyerRepositoryPort {

    Buyer save(Buyer buyer);

    Optional<Buyer> findByIdentification(Buyer buyer);

    boolean existsByIdentification(Buyer buyer);

    boolean existsByEmail(Buyer buyer);

    void update(Buyer buyer);
}
