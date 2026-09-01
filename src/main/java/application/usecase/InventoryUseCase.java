package application.usecase;

import application.dto.InventoryDto;
import application.port.input.InventoryInputPort;
import application.domain.model.entity.InventoryItem;
import application.domain.model.valueobject.StockQuantity;
import application.domain.repository.ProductRepository;
import application.domain.repository.WarehouseRepository;
// Note: Normally we'd also have an InventoryRepository to save the item. 
// For this scaffolding, we'll create the structure.

/**
 * Use Case implementation for Inventory operations.
 */
public class InventoryUseCase implements InventoryInputPort {

    // private final InventoryRepository inventoryRepository; // To be implemented in Domain if needed

    public InventoryUseCase() {
        // Dependencies would be injected here
    }

    @Override
    public InventoryDto registerInventory(Long productId, Long warehouseId, int initialQuantity) {
        InventoryItem item = new InventoryItem(productId, warehouseId, StockQuantity.of(initialQuantity));
        
        // item = inventoryRepository.save(item);
        
        return new InventoryDto(
                item.getId(),
                item.getProductId(),
                item.getWarehouseId(), // Note: InventoryItem doesn't have getWarehouseId yet. I should add it.
                item.getStock().getValue()
        );
    }
}
