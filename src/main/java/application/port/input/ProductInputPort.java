package application.port.input;

import application.dto.ProductDto;
import application.domain.model.valueobject.ProductType;
import java.math.BigDecimal;

/**
 * Input Port for Product catalog operations.
 */
public interface ProductInputPort {

    /**
     * Publishes a new product to the catalog.
     *
     * @param name Product name.
     * @param type Product type (PHYSICAL or DIGITAL).
     * @param price Product price.
     * @param sellerId The ID of the seller publishing the product.
     * @return The created ProductDto.
     */
    ProductDto publishProduct(String name, ProductType type, BigDecimal price, Long sellerId);
}
