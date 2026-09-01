package application.domain.models;

import application.domain.valueobjects.ProductType;
import lombok.NoArgsConstructor;

/**
 * Product delivered immediately after successful payment; it holds no inventory
 * (spec Domain 5).
 */
@NoArgsConstructor
public class DigitalProduct extends Product {

    @Override
    public ProductType getType() {
        return ProductType.DIGITAL;
    }
}
