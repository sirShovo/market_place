package com.marketplace.domain.repository;

import com.marketplace.domain.model.aggregate.Product;
import java.util.Optional;
import java.util.List;

/**
 * Output port interface for Product persistence.
 */
public interface ProductRepository {
    
    /**
     * Saves a product to the repository.
     *
     * @param product The product aggregate to save.
     * @return The persisted product aggregate.
     */
    Product save(Product product);
    
    /**
     * Retrieves a product by its unique identifier.
     *
     * @param id The product's ID.
     * @return An Optional containing the product if found, or empty otherwise.
     */
    Optional<Product> findById(Long id);
    
    /**
     * Retrieves a product by its Stock Keeping Unit (SKU).
     *
     * @param sku The product's SKU.
     * @return An Optional containing the product if found.
     */
    Optional<Product> findBySku(String sku);
    
    /**
     * Retrieves all products that are currently active and available.
     *
     * @return A list of active products.
     */
    List<Product> findAllActive();
    
    /**
     * Retrieves all products associated with a specific category.
     *
     * @param categoryId The ID of the category.
     * @return A list of products within the category.
     */
    List<Product> findByCategoryId(Long categoryId);
}
