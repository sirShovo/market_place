package application.domain.valueobjects;

import java.util.List;
import java.util.Map;

/**
 * Order lifecycle (spec Domain 7). {@code DELIVERED} is terminal and immutable.
 */
public final class OrderStatus extends DomainCatalog {

    public static final OrderStatus CART = new OrderStatus(
            "CART", "Cart", "Provisional product selection.");
    public static final OrderStatus PENDING_PAYMENT = new OrderStatus(
            "PENDING_PAYMENT", "Pending payment", "Awaiting financial confirmation.");
    public static final OrderStatus PAID = new OrderStatus(
            "PAID", "Paid", "Payment confirmed; fulfillment starts.");
    public static final OrderStatus DISPATCHED = new OrderStatus(
            "DISPATCHED", "Dispatched", "Order has physically left the warehouse.");
    public static final OrderStatus DELIVERED = new OrderStatus(
            "DELIVERED", "Delivered", "Delivery confirmed; order finalized and immutable.");

    private static final Map<OrderStatus, List<OrderStatus>> ALLOWED = Map.of(
            CART, List.of(PENDING_PAYMENT),
            PENDING_PAYMENT, List.of(PAID),
            PAID, List.of(DISPATCHED, DELIVERED),
            DISPATCHED, List.of(DELIVERED),
            DELIVERED, List.of());

    private OrderStatus(String code, String name, String description) {
        super(code, name, description);
    }

    /** @return {@code true} if {@code target} is reachable from this status. */
    public boolean canTransitionTo(OrderStatus target) {
        return ALLOWED.getOrDefault(this, List.of()).contains(target);
    }
}
