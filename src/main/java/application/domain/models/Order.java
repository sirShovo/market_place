package application.domain.models;

import application.domain.exceptions.FinalizedOrderModificationException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.ProductType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Formal commercial commitment; its lifecycle is the central process of the system
 * (spec Domain 7). A {@code DELIVERED} order is finalized and immutable (spec §11).
 */
@Getter
@Setter
@NoArgsConstructor
public class Order implements AuditableEntity {

    private String identifier;
    private Buyer buyer;
    private List<OrderItem> items = new ArrayList<>();
    private Money total = Money.zero();
    private OrderStatus status = OrderStatus.CART;
    private LocalDateTime createdAt;

    /** Adds a line and refreshes {@link #getTotal()}. Rejected on a finalized order. */
    public void addItem(OrderItem item) {
        assertNotFinalized();
        this.items.add(item);
        this.total = this.total.add(item.getSubtotal());
    }

    /** Recomputes the total from the current lines. */
    public void recalculateTotal() {
        assertNotFinalized();
        Money sum = Money.zero();
        for (OrderItem item : items) {
            sum = sum.add(item.getSubtotal());
        }
        this.total = sum;
    }

    /**
     * Moves the order to {@code target} if the transition is allowed
     * (see {@link OrderStatus#canTransitionTo}).
     *
     * @throws FinalizedOrderModificationException if the order is already finalized.
     * @throws InvalidStatusTransitionException    if the transition is not allowed.
     */
    public void transitionTo(OrderStatus target) {
        assertNotFinalized();
        if (!this.status.canTransitionTo(target)) {
            throw new InvalidStatusTransitionException(this.status.getCode(), target.getCode());
        }
        this.status = target;
    }

    /** @return {@code true} if the order is finalized ({@code DELIVERED}). */
    public boolean isFinalized() {
        return OrderStatus.DELIVERED.equals(this.status);
    }

    /** @return {@code true} if any line references a physical product. */
    public boolean containsPhysicalProducts() {
        return items.stream()
                .anyMatch(item -> ProductType.PHYSICAL.equals(item.getProduct().getType()));
    }

    private void assertNotFinalized() {
        if (isFinalized()) {
            throw new FinalizedOrderModificationException(identifier);
        }
    }

    @Override
    public String auditableType() {
        return "Order";
    }

    @Override
    public String auditableId() {
        return identifier;
    }
}
