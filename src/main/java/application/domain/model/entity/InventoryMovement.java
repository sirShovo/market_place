package application.domain.model.entity;
import application.domain.model.valueobject.MovementType;
import java.time.LocalDateTime;

/**
 * Domain entity representing a transactional movement in the inventory.
 */
public class InventoryMovement {
    private Long id;
    private Long inventoryItemId;
    private MovementType type;
    private int quantity;
    private LocalDateTime timestamp;
    
    /**
     * Constructor for InventoryMovement.
     * @param inventoryItemId The ID of the affected inventory item.
     * @param type The type of movement (e.g., ENTRY, RESERVE).
     * @param quantity The movement quantity.
     */
    public InventoryMovement(Long inventoryItemId, MovementType type, int quantity) {
        this.inventoryItemId = inventoryItemId; this.type = type; this.quantity = quantity; this.timestamp = LocalDateTime.now();
    }
    
    /** @return The InventoryMovement ID */
    public Long getId() { return id; }
    /** @param id The InventoryMovement ID to set */
    public void setId(Long id) { this.id = id; }
    
    /** @return The movement type */
    public MovementType getType() { return type; }
}
