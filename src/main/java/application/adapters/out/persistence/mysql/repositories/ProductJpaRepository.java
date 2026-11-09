package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.ProductJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, String> {

    List<ProductJpaEntity> findBySeller_Identification(String sellerIdentification);

    List<ProductJpaEntity> findByStatus(String status);
}
