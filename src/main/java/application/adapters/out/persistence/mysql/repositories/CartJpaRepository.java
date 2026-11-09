package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.CartJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartJpaRepository extends JpaRepository<CartJpaEntity, Long> {

    Optional<CartJpaEntity> findByBuyer_IdentificationAndStatus(String buyerIdentification, String status);
}
