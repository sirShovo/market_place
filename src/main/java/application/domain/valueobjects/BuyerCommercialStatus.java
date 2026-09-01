package application.domain.valueobjects;

/**
 * Ability of a {@code Buyer} to operate commercially (spec Domain 2).
 */
public final class BuyerCommercialStatus extends DomainCatalog {

    public static final BuyerCommercialStatus ACTIVE = new BuyerCommercialStatus(
            "ACTIVE", "Active", "Buyer can place orders.");
    public static final BuyerCommercialStatus SUSPENDED = new BuyerCommercialStatus(
            "SUSPENDED", "Suspended", "Buyer temporarily cannot place orders.");

    private BuyerCommercialStatus(String code, String name, String description) {
        super(code, name, description);
    }
}
