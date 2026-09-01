package application.domain.service;

import application.domain.model.entity.InventoryItem;
import application.domain.model.entity.InventoryMovement;
import application.domain.model.valueobject.MovementType;
import application.domain.exception.InvalidReservationException;

/**
 * Domain Service for managing inventory logic.
 * Handles the creation of transactional audit logs (InventoryMovement) 
 * when reserving or adjusting stock.
 */
public class InventoryDomainService {

    /**
     * Reserves stock for an inventory item.
     * Decreases the available stock and generates an InventoryMovement record.
     *
     * @param inventoryItem The inventory item to update.
     * @param quantity      The amount of stock to reserve.
     * @return The resulting InventoryMovement log.
     * @throws InvalidReservationException If the reservation cannot be fulfilled.
     */
    public InventoryMovement reserveStock(InventoryItem inventoryItem, int quantity) {
        if (quantity <= 0) {
            throw new InvalidReservationException("Reservation quantity must be positive.");
        }

        // Subtract stock (this will throw NegativeStockException if insufficient)
        inventoryItem.setStock(inventoryItem.getStock().subtract(quantity));

        // Create the audit log for the reservation
        return new InventoryMovement(
                inventoryItem.getId(),
                MovementType.RESERVE,
                quantity
        );
    }

    /**
     * Finalizes a sale by marking reserved stock as officially exited.
     * Note: In a real system, we might need a separate 'reserved quantity' field,
     * but for this exercise we simply log the SALE movement to complete the transaction.
     *
     * @param inventoryItem The inventory item.
     * @param quantity      The amount sold.
     * @return The resulting InventoryMovement log.
     */
    public InventoryMovement finalizeSale(InventoryItem inventoryItem, int quantity) {
        return new InventoryMovement(
                inventoryItem.getId(),
                MovementType.SALE,
                quantity
        );
    }
}
