package application.domain.valueobjects;

/**
 * Operational status of a {@code Seller} (spec Domain 3).
 */
public final class SellerStatus extends DomainCatalog {

    public static final SellerStatus ACTIVE = new SellerStatus(
            "ACTIVE", "Active", "Seller can publish and manage products.");
    public static final SellerStatus SUSPENDED = new SellerStatus(
            "SUSPENDED", "Suspended", "Seller cannot publish or manage products.");

    private SellerStatus(String code, String name, String description) {
        super(code, name, description);
    }
}
