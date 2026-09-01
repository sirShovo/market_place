package application.domain.ports.out;

import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Product;
import java.util.List;
import java.util.Optional;

/** Persistence contract for {@link InventoryItem} and its {@link InventoryMovement} trail. */
public interface InventoryRepositoryPort {

    InventoryItem save(InventoryItem item);

    Optional<InventoryItem> findByProductAndWarehouse(InventoryItem probe);

    List<InventoryItem> findByProduct(Product product);

    void update(InventoryItem item);

    InventoryMovement saveMovement(InventoryMovement movement);

    List<InventoryMovement> findMovements(InventoryItem item);
}
