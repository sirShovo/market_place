package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.OperationJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationJpaRepository extends JpaRepository<OperationJpaEntity, Integer> {

    List<OperationJpaEntity> findByPerformedBy_Username(String username);

    List<OperationJpaEntity> findByAffectedEntityTypeAndAffectedEntityId(
            String affectedEntityType, String affectedEntityId);
}
