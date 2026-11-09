package application.adapters.in.rest.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record UpdateBuyerRequest(
        @NotBlank(message = "Full name is required") String fullName,
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
        String phoneNumber,
        String address,
        @NotBlank(message = "Main address is required") String mainAddress,
        List<String> additionalAddresses
) {
}
