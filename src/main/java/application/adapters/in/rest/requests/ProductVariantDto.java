package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;

public record ProductVariantDto(
        @NotBlank(message = "Attribute name is required") String attributeName,
        @NotBlank(message = "Value is required") String value
) {
}
