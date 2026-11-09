package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.UserJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Integer> {

    Optional<UserJpaEntity> findByUsername(String username);

    Optional<UserJpaEntity> findByIdentification(String identification);

    boolean existsByIdentification(String identification);

    boolean existsByEmail(String email);
}
