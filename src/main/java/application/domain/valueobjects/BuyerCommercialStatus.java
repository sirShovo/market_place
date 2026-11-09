package application.domain.valueobjects;

import application.domain.exceptions.DomainException;
import java.util.List;

/**
 * Ability of a {@code Buyer} to operate commercially (spec Domain 2).
 */
public final class BuyerCommercialStatus extends DomainCatalog {

    public static final BuyerCommercialStatus ACTIVE = new BuyerCommercialStatus(
            "ACTIVE", "Active", "Buyer can place orders.");
    public static final BuyerCommercialStatus SUSPENDED = new BuyerCommercialStatus(
            "SUSPENDED", "Suspended", "Buyer temporarily cannot place orders.");

    private static final List<BuyerCommercialStatus> VALUES = List.of(ACTIVE, SUSPENDED);

    private BuyerCommercialStatus(String code, String name, String description) {
        super(code, name, description);
    }

    public static BuyerCommercialStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        return VALUES.stream()
                .filter(v -> v.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new DomainException("Unknown BuyerCommercialStatus code: " + code));
    }
}
