package application.domain.valueobjects;

/**
 * Fulfillment nature of a catalog item (spec Domain 5).
 */
public final class ProductType extends DomainCatalog {

    public static final ProductType PHYSICAL = new ProductType(
            "PHYSICAL", "Physical product", "Requires inventory and dispatch.");
    public static final ProductType DIGITAL = new ProductType(
            "DIGITAL", "Digital product", "Delivered immediately after payment.");

    private ProductType(String code, String name, String description) {
        super(code, name, description);
    }
}
