package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;

public record ChangeStatusRequest(
        @NotBlank(message = "Status is required") String status
) {
}
