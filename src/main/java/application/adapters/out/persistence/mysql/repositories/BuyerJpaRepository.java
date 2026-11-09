package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.BuyerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyerJpaRepository extends JpaRepository<BuyerJpaEntity, String> {

    boolean existsByIdentification(String identification);

    boolean existsByEmail(String email);
}
