package application.adapters.in.rest.responses;

import java.util.List;

public record SellerResponse(
        String identification,
        String fullName,
        String email,
        String phoneNumber,
        String address,
        String status,
        List<WarehouseResponse> warehouses
) {
}
