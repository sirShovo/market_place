package application.dto;

/**
 * Data Transfer Object for Inventory representation.
 */
public record InventoryDto(
        Long id,
        Long productId,
        Long warehouseId,
        int stockQuantity
) {}
