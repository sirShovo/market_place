package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.InventoryItemJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryItemJpaRepository extends JpaRepository<InventoryItemJpaEntity, Long> {

    Optional<InventoryItemJpaEntity> findByProduct_IdentifierAndWarehouse_Identifier(
            String productIdentifier, String warehouseIdentifier);

    List<InventoryItemJpaEntity> findByProduct_Identifier(String productIdentifier);
}
