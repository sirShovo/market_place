package domain.model.entity;
import domain.model.valueobject.StockQuantity;
public class InventoryItem {
    private Long id;
    private Long productId;
    private Long warehouseId;
    private StockQuantity stock;
    public InventoryItem(Long productId, Long warehouseId, StockQuantity stock) {
        this.productId = productId; this.warehouseId = warehouseId; this.stock = stock;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public StockQuantity getStock() { return stock; }
}
