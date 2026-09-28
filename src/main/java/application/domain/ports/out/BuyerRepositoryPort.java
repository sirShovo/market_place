package application.domain.ports.out;

import application.domain.models.Buyer;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import java.util.Optional;

/** Persistence contract for {@link Buyer}. */
public interface BuyerRepositoryPort {

    Buyer save(Buyer buyer);

    Optional<Buyer> findByIdentification(Buyer buyer);

    /** Platform-wide uniqueness check (spec §11). */
    boolean existsByIdentification(DocumentId identification);

    /** Platform-wide uniqueness check (spec §11). */
    boolean existsByEmail(Email email);

    void update(Buyer buyer);
}
