package domain.repository;
import domain.model.entity.Warehouse;
import java.util.Optional;

/**
 * Output port interface for Warehouse persistence.
 */
public interface WarehouseRepository { 
    /**
     * Saves a warehouse to the repository.
     * @param warehouse The warehouse entity to save.
     * @return The persisted warehouse entity.
     */
    Warehouse save(Warehouse warehouse); 
    
    /**
     * Retrieves a warehouse by its unique identifier.
     * @param id The warehouse's ID.
     * @return An Optional containing the warehouse if found.
     */
    Optional<Warehouse> findById(Long id); 
}
