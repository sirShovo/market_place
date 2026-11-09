package application.domain.valueobjects;

import application.domain.exceptions.DomainException;
import java.util.List;

/**
 * Fulfillment nature of a catalog item (spec Domain 5).
 */
public final class ProductType extends DomainCatalog {

    public static final ProductType PHYSICAL = new ProductType(
            "PHYSICAL", "Physical product", "Requires inventory and dispatch.");
    public static final ProductType DIGITAL = new ProductType(
            "DIGITAL", "Digital product", "Delivered immediately after payment.");

    private static final List<ProductType> VALUES = List.of(PHYSICAL, DIGITAL);

    private ProductType(String code, String name, String description) {
        super(code, name, description);
    }

    public static ProductType fromCode(String code) {
        if (code == null) {
            return null;
        }
        return VALUES.stream()
                .filter(v -> v.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new DomainException("Unknown ProductType code: " + code));
    }
}
