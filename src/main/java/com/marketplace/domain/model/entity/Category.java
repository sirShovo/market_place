package com.marketplace.domain.model.entity;

import com.marketplace.domain.exception.DomainValidationException;

/**
 * Domain entity representing a product Category.
 * 
 * Organizes products into navigable hierarchies. Categories can be activated
 * or deactivated by administrators to control their visibility.
 */
public class Category {
    private Long id;
    private String name;
    private String description;
    private boolean active;

    /**
     * Default constructor for JPA and Mappers. Should not be used directly for domain logic.
     */
    protected Category() {}

    private Category(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("Category name cannot be empty");
        }
        this.name = name;
        this.description = description;
        this.active = true;
    }

    /**
     * Factory method to create a new Category.
     *
     * @param name The name of the category.
     * @param description A brief description of the category.
     * @return A new Category entity instance.
     */
    public static Category create(String name, String description) {
        return new Category(name, description);
    }

    /**
     * Updates the category details.
     *
     * @param name The new name.
     * @param description The new description.
     */
    public void update(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("Category name cannot be empty");
        }
        this.name = name;
        this.description = description;
    }

    /**
     * Deactivates the category, making it hidden or unavailable for new products.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Activates the category.
     */
    public void activate() {
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
}
