package application.domain.repository;
import application.domain.model.aggregate.Order;
import java.util.Optional;

/**
 * Output port interface for Order persistence.
 */
public interface OrderRepository { 
    /**
     * Saves an order to the repository.
     * @param order The order aggregate to save.
     * @return The persisted order aggregate.
     */
    Order save(Order order); 
    
    /**
     * Retrieves an order by its unique identifier.
     * @param id The order's ID.
     * @return An Optional containing the order if found.
     */
    Optional<Order> findById(Long id); 
}
