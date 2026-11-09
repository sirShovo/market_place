package application.domain.valueobjects;

import application.domain.exceptions.DomainException;
import java.util.List;

/**
 * Classification of storage locations (spec Domain 4).
 */
public final class WarehouseType extends DomainCatalog {

    public static final WarehouseType MARKETPLACE = new WarehouseType(
            "MARKETPLACE", "Marketplace warehouse", "Operated directly by the marketplace.");
    public static final WarehouseType SELLER = new WarehouseType(
            "SELLER", "Seller warehouse", "Owned and operated by a specific seller.");

    private static final List<WarehouseType> VALUES = List.of(MARKETPLACE, SELLER);

    private WarehouseType(String code, String name, String description) {
        super(code, name, description);
    }

    public static WarehouseType fromCode(String code) {
        if (code == null) {
            return null;
        }
        return VALUES.stream()
                .filter(v -> v.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new DomainException("Unknown WarehouseType code: " + code));
    }
}
