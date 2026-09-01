package application.domain.valueobjects;

/**
 * Responsibilities and permissions of a participant. Exactly one role per participant
 * (spec §5, RG02).
 */
public final class UserRole extends DomainCatalog {

    public static final UserRole BUYER = new UserRole(
            "BUYER", "Buyer", "Acquires published products.");
    public static final UserRole SELLER = new UserRole(
            "SELLER", "Seller", "Registers and manages its own products.");
    public static final UserRole LOGISTICS_OPERATOR = new UserRole(
            "LOGISTICS_OPERATOR", "Logistics Operator", "Runs physical warehouse and dispatch operations.");
    public static final UserRole ADMIN = new UserRole(
            "ADMIN", "Administrator", "Administers sellers and warehouses.");
    public static final UserRole SUPERVISOR = new UserRole(
            "SUPERVISOR", "Supervisor", "Read-only operational monitoring profile.");

    private UserRole(String code, String name, String description) {
        super(code, name, description);
    }
}
