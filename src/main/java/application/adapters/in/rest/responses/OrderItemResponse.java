package application.adapters.in.rest.responses;

import java.math.BigDecimal;

public record OrderItemResponse(
        String productIdentifier,
        String productName,
        String variantAttributeName,
        String variantValue,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        String warehouseIdentifier
) {
}
