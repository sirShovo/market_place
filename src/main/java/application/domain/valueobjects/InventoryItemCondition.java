package application.domain.valueobjects;

/**
 * Condition of the stock held by an {@code InventoryItem} (spec §11).
 */
public final class InventoryItemCondition extends DomainCatalog {

    public static final InventoryItemCondition AVAILABLE = new InventoryItemCondition(
            "AVAILABLE", "Available", "Stock can be reserved and sold.");
    public static final InventoryItemCondition DAMAGED = new InventoryItemCondition(
            "DAMAGED", "Damaged", "Stock cannot be reserved.");

    private InventoryItemCondition(String code, String name, String description) {
        super(code, name, description);
    }
}
