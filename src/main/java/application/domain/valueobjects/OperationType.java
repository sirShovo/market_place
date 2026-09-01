package application.domain.valueobjects;

/**
 * Significant business operations recorded in the audit trail.
 */
public final class OperationType extends DomainCatalog {

    public static final OperationType USER_REGISTRATION = new OperationType(
            "USER_REGISTRATION", "User registration", "A new user was registered.");
    public static final OperationType USER_STATUS_CHANGE = new OperationType(
            "USER_STATUS_CHANGE", "User status change", "A user's access status changed.");
    public static final OperationType SELLER_ONBOARDING = new OperationType(
            "SELLER_ONBOARDING", "Seller onboarding", "A seller and its first warehouse were registered.");
    public static final OperationType WAREHOUSE_REGISTRATION = new OperationType(
            "WAREHOUSE_REGISTRATION", "Warehouse registration", "A warehouse was registered.");
    public static final OperationType PRODUCT_PUBLICATION = new OperationType(
            "PRODUCT_PUBLICATION", "Product publication", "A product was published to the catalog.");
    public static final OperationType PRODUCT_STATUS_CHANGE = new OperationType(
            "PRODUCT_STATUS_CHANGE", "Product status change", "A product's catalog status changed.");
    public static final OperationType INVENTORY_ENTRY = new OperationType(
            "INVENTORY_ENTRY", "Inventory entry", "Stock was added to a warehouse.");
    public static final OperationType INVENTORY_RESERVATION = new OperationType(
            "INVENTORY_RESERVATION", "Inventory reservation", "Stock was reserved for an order.");
    public static final OperationType INVENTORY_RELEASE = new OperationType(
            "INVENTORY_RELEASE", "Inventory release", "A previous reservation was released.");
    public static final OperationType INVENTORY_ADJUSTMENT = new OperationType(
            "INVENTORY_ADJUSTMENT", "Inventory adjustment", "Stock levels were manually corrected.");
    public static final OperationType INVENTORY_RETURN = new OperationType(
            "INVENTORY_RETURN", "Inventory return", "Stock was returned to a warehouse.");
    public static final OperationType CART_CHECKOUT = new OperationType(
            "CART_CHECKOUT", "Cart checkout", "A cart was converted into an order.");
    public static final OperationType ORDER_PAYMENT = new OperationType(
            "ORDER_PAYMENT", "Order payment", "An order payment was processed.");
    public static final OperationType ORDER_DISPATCH = new OperationType(
            "ORDER_DISPATCH", "Order dispatch", "An order was dispatched from the warehouse.");
    public static final OperationType ORDER_DELIVERY = new OperationType(
            "ORDER_DELIVERY", "Order delivery", "An order delivery was confirmed.");

    private OperationType(String code, String name, String description) {
        super(code, name, description);
    }
}
