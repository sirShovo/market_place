package application.domain.valueobjects;

import application.domain.exceptions.DomainException;
import java.util.List;

/**
 * Operational status of a {@code Seller} (spec Domain 3).
 */
public final class SellerStatus extends DomainCatalog {

    public static final SellerStatus ACTIVE = new SellerStatus(
            "ACTIVE", "Active", "Seller can publish and manage products.");
    public static final SellerStatus SUSPENDED = new SellerStatus(
            "SUSPENDED", "Suspended", "Seller cannot publish or manage products.");

    private static final List<SellerStatus> VALUES = List.of(ACTIVE, SUSPENDED);

    private SellerStatus(String code, String name, String description) {
        super(code, name, description);
    }

    public static SellerStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        return VALUES.stream()
                .filter(v -> v.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new DomainException("Unknown SellerStatus code: " + code));
    }
}
