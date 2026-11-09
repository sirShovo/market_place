package application.adapters.in.rest.responses;

import application.adapters.in.rest.requests.ProductVariantDto;
import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        String identifier,
        String name,
        String description,
        String type,
        String status,
        BigDecimal price,
        String sellerIdentification,
        Long categoryId,
        String categoryName,
        List<ProductVariantDto> variants
) {
}
