package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.OrderJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, String> {

    List<OrderJpaEntity> findByBuyer_Identification(String buyerIdentification);
}
