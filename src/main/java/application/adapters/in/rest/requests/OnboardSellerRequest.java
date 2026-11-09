package application.adapters.in.rest.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OnboardSellerRequest(
        @NotBlank(message = "Identification is required") String identification,
        @NotBlank(message = "Full name is required") String fullName,
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
        String phoneNumber,
        String address,
        @NotBlank(message = "Username is required") String username,
        @NotBlank(message = "Password is required") String password,
        @NotBlank(message = "Warehouse identifier is required") String warehouseIdentifier,
        @NotBlank(message = "Warehouse name is required") String warehouseName,
        String warehouseLocation
) {
}
