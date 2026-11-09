package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {
}
