package application.domain.valueobjects;

/**
 * Lifecycle of a shopping {@code Cart} (spec Domain 7, state 1).
 */
public final class CartStatus extends DomainCatalog {

    public static final CartStatus ACTIVE = new CartStatus(
            "ACTIVE", "Active", "Cart is being edited by the buyer.");
    public static final CartStatus CONVERTED = new CartStatus(
            "CONVERTED", "Converted", "Cart was turned into an order at checkout.");
    public static final CartStatus ABANDONED = new CartStatus(
            "ABANDONED", "Abandoned", "Cart was discarded.");

    private CartStatus(String code, String name, String description) {
        super(code, name, description);
    }
}
