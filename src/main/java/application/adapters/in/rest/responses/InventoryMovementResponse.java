package application.adapters.in.rest.responses;

import java.time.LocalDateTime;

public record InventoryMovementResponse(
        Long id,
        Long inventoryItemId,
        String type,
        int quantity,
        LocalDateTime occurredOn,
        String performedByUsername
) {
}
