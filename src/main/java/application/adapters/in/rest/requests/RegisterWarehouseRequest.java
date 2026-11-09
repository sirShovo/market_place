package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;

public record RegisterWarehouseRequest(
        @NotBlank(message = "Identifier is required") String identifier,
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Type is required") String type,
        String ownerIdentification,
        String location
) {
}
