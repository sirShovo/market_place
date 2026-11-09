package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.SellerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerJpaRepository extends JpaRepository<SellerJpaEntity, String> {

    boolean existsByIdentification(String identification);

    boolean existsByEmail(String email);
}
