package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record InventoryOperationRequest(
        @NotBlank(message = "Product identifier is required") String productIdentifier,
        @NotBlank(message = "Warehouse identifier is required") String warehouseIdentifier,
        @PositiveOrZero(message = "Quantity must be non-negative") int quantity,
        String condition
) {
}
