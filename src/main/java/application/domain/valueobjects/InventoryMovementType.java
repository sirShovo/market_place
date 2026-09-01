package application.domain.valueobjects;

/**
 * Type of stock movement recorded for traceability (spec Domain 6).
 */
public final class InventoryMovementType extends DomainCatalog {

    public static final InventoryMovementType ENTRY = new InventoryMovementType(
            "ENTRY", "Entry", "Incoming stock added to a warehouse.");
    public static final InventoryMovementType RESERVATION = new InventoryMovementType(
            "RESERVATION", "Reservation", "Stock reserved for an order.");
    public static final InventoryMovementType SALE_EXIT = new InventoryMovementType(
            "SALE_EXIT", "Sale exit", "Stock leaving the warehouse due to a sale.");
    public static final InventoryMovementType ADJUSTMENT = new InventoryMovementType(
            "ADJUSTMENT", "Adjustment", "Manual correction of stock levels.");
    public static final InventoryMovementType RETURN = new InventoryMovementType(
            "RETURN", "Return", "Stock returned to a warehouse.");

    private InventoryMovementType(String code, String name, String description) {
        super(code, name, description);
    }
}
