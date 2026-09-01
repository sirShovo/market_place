package application.domain.models;

import application.domain.valueobjects.ProductType;
import lombok.NoArgsConstructor;

/**
 * Product that requires inventory and dispatch. An order containing at least one
 * physical product stays {@code PAID} until a logistics operator dispatches it
 * (spec Domain 5).
 */
@NoArgsConstructor
public class PhysicalProduct extends Product {

    @Override
    public ProductType getType() {
        return ProductType.PHYSICAL;
    }
}
