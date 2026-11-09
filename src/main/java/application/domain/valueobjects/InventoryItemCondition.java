package application.domain.valueobjects;

import application.domain.exceptions.DomainException;
import java.util.List;

/**
 * Condition of the stock held by an {@code InventoryItem} (spec §11).
 */
public final class InventoryItemCondition extends DomainCatalog {

    public static final InventoryItemCondition AVAILABLE = new InventoryItemCondition(
            "AVAILABLE", "Available", "Stock can be reserved and sold.");
    public static final InventoryItemCondition DAMAGED = new InventoryItemCondition(
            "DAMAGED", "Damaged", "Stock cannot be reserved.");

    private static final List<InventoryItemCondition> VALUES = List.of(AVAILABLE, DAMAGED);

    private InventoryItemCondition(String code, String name, String description) {
        super(code, name, description);
    }

    public static InventoryItemCondition fromCode(String code) {
        if (code == null) {
            return null;
        }
        return VALUES.stream()
                .filter(v -> v.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new DomainException("Unknown InventoryItemCondition code: " + code));
    }
}
