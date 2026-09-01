package application.domain.valueobjects;

/**
 * Classification of storage locations (spec Domain 4).
 */
public final class WarehouseType extends DomainCatalog {

    public static final WarehouseType MARKETPLACE = new WarehouseType(
            "MARKETPLACE", "Marketplace warehouse", "Operated directly by the marketplace.");
    public static final WarehouseType SELLER = new WarehouseType(
            "SELLER", "Seller warehouse", "Owned and operated by a specific seller.");

    private WarehouseType(String code, String name, String description) {
        super(code, name, description);
    }
}
