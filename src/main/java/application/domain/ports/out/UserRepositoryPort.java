package application.domain.ports.out;

import application.domain.models.User;
import java.util.Optional;

/** Persistence contract for {@link User}. */
public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findByUsername(User user);

    Optional<User> findByIdentification(User user);

    boolean existsByIdentification(User user);

    boolean existsByEmail(User user);

    void update(User user);
}
