package application.domain.repository;
import application.domain.model.aggregate.Product;
import java.util.Optional;

/**
 * Output port interface for Product persistence.
 */
public interface ProductRepository { 
    /**
     * Saves a product to the repository.
     * @param product The product aggregate to save.
     * @return The persisted product aggregate.
     */
    Product save(Product product); 
    
    /**
     * Retrieves a product by its unique identifier.
     * @param id The product's ID.
     * @return An Optional containing the product if found.
     */
    Optional<Product> findById(Long id); 
}
