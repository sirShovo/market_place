package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CartItemRequest(
        @NotBlank(message = "Product identifier is required") String productIdentifier,
        String variantAttributeName,
        String variantValue,
        @Positive(message = "Quantity must be positive") int quantity
) {
}
