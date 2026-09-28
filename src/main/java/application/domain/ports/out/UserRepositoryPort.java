package application.domain.ports.out;

import application.domain.models.User;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import java.util.Optional;

/** Persistence contract for {@link User}. */
public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findByUsername(User user);

    Optional<User> findByIdentification(User user);

    /**
     * Platform-wide uniqueness check (spec §11), used by
     * {@code ValidatePlatformUniquenessService} across every participant type.
     */
    boolean existsByIdentification(DocumentId identification);

    /** Platform-wide uniqueness check (spec §11). */
    boolean existsByEmail(Email email);

    void update(User user);
}
