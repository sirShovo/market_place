package application.adapters.in.rest.responses;

public record InventoryItemResponse(
        Long id,
        String productIdentifier,
        String warehouseIdentifier,
        int stock,
        String condition
) {
}
