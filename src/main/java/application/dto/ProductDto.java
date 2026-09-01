package application.dto;

import application.domain.model.valueobject.ProductType;
import java.math.BigDecimal;

/**
 * Data Transfer Object for Product representation.
 */
public record ProductDto(
        Long id,
        String name,
        ProductType type,
        BigDecimal price,
        Long sellerId
) {}
