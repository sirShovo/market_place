package application.domain.ports.out;

import application.domain.models.Seller;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import java.util.List;
import java.util.Optional;

/** Persistence contract for {@link Seller}. */
public interface SellerRepositoryPort {

    Seller save(Seller seller);

    Optional<Seller> findByIdentification(Seller seller);

    /** Platform-wide uniqueness check (spec §11). */
    boolean existsByIdentification(DocumentId identification);

    /** Platform-wide uniqueness check (spec §11). */
    boolean existsByEmail(Email email);

    List<Seller> findAll();

    void update(Seller seller);
}
