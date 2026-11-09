package application.adapters.in.rest.responses;

import java.util.List;

public record BuyerResponse(
        String identification,
        String fullName,
        String email,
        String phoneNumber,
        String address,
        String commercialStatus,
        String mainAddress,
        List<String> additionalAddresses
) {
}
