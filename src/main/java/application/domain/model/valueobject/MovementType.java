package application.domain.model.valueobject;

/**
 * Enumeration representing MovementType states/types.
 */
public enum MovementType {
    /** Stock increased via entry */
    ENTRY,
    /** Stock reserved for an order */
    RESERVE,
    /** Stock permanently exited due to a sale */
    SALE,
    /** Stock manually adjusted */
    ADJUST,
    /** Stock increased due to a return */
    RETURN
}
