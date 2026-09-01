package application.domain.model.entity;

/**
 * Domain entity representing a product category.
 */
public class Category {
    private Long id;
    private String name;
    
    /**
     * Constructor for Category.
     * @param name The category name.
     */
    public Category(String name) { this.name = name; }
    
    /** @return The Category ID */
    public Long getId() { return id; }
    /** @param id The Category ID to set */
    public void setId(Long id) { this.id = id; }
    
    /** @return The category name */
    public String getName() { return name; }
}
