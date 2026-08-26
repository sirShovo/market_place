package application.port.input;

import application.dto.InventoryDto;

/**
 * Input Port for Inventory management operations.
 */
public interface InventoryInputPort {

    /**
     * Adds initial stock to a product in a specific warehouse.
     *
     * @param productId The product ID.
     * @param warehouseId The warehouse ID.
     * @param initialQuantity The initial stock quantity.
     * @return The created InventoryDto.
     */
    InventoryDto registerInventory(Long productId, Long warehouseId, int initialQuantity);
}
