package application.adapters.in.rest.responses;

import java.util.List;

public record CartResponse(
        Long id,
        String buyerIdentification,
        String status,
        List<CartItemResponse> items
) {
}
