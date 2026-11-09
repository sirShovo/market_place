package application.adapters.in.rest.responses;

public record CartItemResponse(
        String productIdentifier,
        String productName,
        String variantAttributeName,
        String variantValue,
        int quantity
) {
}
