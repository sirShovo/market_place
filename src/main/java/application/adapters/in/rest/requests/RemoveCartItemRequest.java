package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;

public record RemoveCartItemRequest(
        @NotBlank(message = "Product identifier is required") String productIdentifier,
        String variantAttributeName,
        String variantValue
) {
}
