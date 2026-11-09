package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

public record PublishProductRequest(
        @NotBlank(message = "Identifier is required") String identifier,
        @NotBlank(message = "Name is required") String name,
        String description,
        @NotBlank(message = "Product type is required") String type,
        @NotNull(message = "Price is required") @PositiveOrZero(message = "Price must be non-negative") BigDecimal price,
        String sellerIdentification,
        Long categoryId,
        String categoryName,
        List<ProductVariantDto> variants
) {
}
