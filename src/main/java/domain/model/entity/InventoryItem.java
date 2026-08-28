package domain.model.entity;
import domain.model.valueobject.StockQuantity;

/**
 * Domain entity mapping a product to a specific warehouse with a stock level.
 */
public class InventoryItem {
    private Long id;
    private Long productId;
    private Long warehouseId;
    private StockQuantity stock;
    
    /**
     * Constructor for InventoryItem.
     * @param productId The associated product ID.
     * @param warehouseId The associated warehouse ID.
     * @param stock The current stock quantity.
     */
    public InventoryItem(Long productId, Long warehouseId, StockQuantity stock) {
        this.productId = productId; this.warehouseId = warehouseId; this.stock = stock;
    }
    
    /** @return The InventoryItem ID */
    public Long getId() { return id; }
    /** @param id The InventoryItem ID to set */
    public void setId(Long id) { this.id = id; }
    
    /** @return The product ID */
    public Long getProductId() { return productId; }
    
    /** @return The stock quantity object */
    public StockQuantity getStock() { return stock; }
    
    /** @param stock The stock quantity object to set */
    public void setStock(StockQuantity stock) { this.stock = stock; }
}
