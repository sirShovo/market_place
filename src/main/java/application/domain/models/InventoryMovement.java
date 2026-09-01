package application.domain.models;

import application.domain.valueobjects.InventoryMovementType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Immutable traceability record for every stock change (spec Domain 6).
 */
@Getter
@Setter
@NoArgsConstructor
public class InventoryMovement {

    private Long id;
    private InventoryItem inventoryItem;
    private InventoryMovementType type;
    private int quantity;
    private LocalDateTime occurredOn;
    private User performedBy;
}
