package com.marketplace.domain.repository;

import com.marketplace.domain.model.entity.Category;
import java.util.Optional;
import java.util.List;

/**
 * Output port interface for Category persistence.
 */
public interface CategoryRepository {
    
    /**
     * Saves a category to the repository.
     *
     * @param category The category entity to save.
     * @return The persisted category entity.
     */
    Category save(Category category);
    
    /**
     * Retrieves a category by its unique identifier.
     *
     * @param id The category's ID.
     * @return An Optional containing the category if found, or empty otherwise.
     */
    Optional<Category> findById(Long id);
    
    /**
     * Retrieves a category by its exact name.
     *
     * @param name The name of the category.
     * @return An Optional containing the category if found.
     */
    Optional<Category> findByName(String name);
    
    /**
     * Retrieves all categories that are marked as active.
     *
     * @return A list of active categories.
     */
    List<Category> findAllActive();
}
