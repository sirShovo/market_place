package application.domain.model.entity;

/**
 * Domain entity representing a physical warehouse.
 */
public class Warehouse {
    private Long id;
    private String name;
    private Long sellerId;
    
    /**
     * Constructor for Warehouse.
     * @param name The warehouse name.
     * @param sellerId The ID of the seller who owns the warehouse.
     */
    public Warehouse(String name, Long sellerId) { this.name = name; this.sellerId = sellerId; }
    
    /** @return The Warehouse ID */
    public Long getId() { return id; }
    /** @param id The Warehouse ID to set */
    public void setId(Long id) { this.id = id; }
    
    /** @return The warehouse name */
    public String getName() { return name; }
}
