package application.domain.valueobjects;

import application.domain.exceptions.DomainException;
import java.util.List;

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

    private static final List<ProductStatus> VALUES = List.of(PUBLISHED, SUSPENDED, DISCONTINUED);

    private ProductStatus(String code, String name, String description) {
        super(code, name, description);
    }

    public static ProductStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        return VALUES.stream()
                .filter(v -> v.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new DomainException("Unknown ProductStatus code: " + code));
    }
}
