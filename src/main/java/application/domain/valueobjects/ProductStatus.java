package application.domain.valueobjects;

/**
 * Catalog visibility of a product (spec Domain 5).
 */
public final class ProductStatus extends DomainCatalog {

    public static final ProductStatus PUBLISHED = new ProductStatus(
            "PUBLISHED", "Published", "Visible in the public catalog.");
    public static final ProductStatus SUSPENDED = new ProductStatus(
            "SUSPENDED", "Suspended", "Temporarily hidden from the catalog.");
    public static final ProductStatus DISCONTINUED = new ProductStatus(
            "DISCONTINUED", "Discontinued", "Permanently removed from the catalog.");

    private ProductStatus(String code, String name, String description) {
        super(code, name, description);
    }
}
