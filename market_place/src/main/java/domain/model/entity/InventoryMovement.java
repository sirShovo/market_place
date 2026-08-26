package domain.model.entity;
import domain.model.valueobject.MovementType;
import java.time.LocalDateTime;
public class InventoryMovement {
    private Long id;
    private Long inventoryItemId;
    private MovementType type;
    private int quantity;
    private LocalDateTime timestamp;
    public InventoryMovement(Long inventoryItemId, MovementType type, int quantity) {
        this.inventoryItemId = inventoryItemId; this.type = type; this.quantity = quantity; this.timestamp = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}
