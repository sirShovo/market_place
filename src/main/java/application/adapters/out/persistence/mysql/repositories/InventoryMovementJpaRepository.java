package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.InventoryMovementJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryMovementJpaRepository extends JpaRepository<InventoryMovementJpaEntity, Long> {

    List<InventoryMovementJpaEntity> findByInventoryItem_Id(Long inventoryItemId);
}
