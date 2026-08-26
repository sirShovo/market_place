package domain.model.aggregate;
import domain.model.entity.OrderItem;
import domain.model.valueobject.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain aggregate root representing a purchase Order.
 */
public class Order {
    private Long id;
    private Long buyerId;
    private OrderStatus status;
    private List<OrderItem> items;
    private Money total;
    
    /**
     * Constructor for Order.
     * @param buyerId The ID of the buyer making the purchase.
     */
    public Order(Long buyerId) {
        this.buyerId = buyerId; this.status = OrderStatus.CART; this.items = new ArrayList<>(); this.total = Money.zero();
    }
    
    /**
     * Adds an item to the order and updates the total amount.
     * @param item The OrderItem to add.
     */
    public void addItem(OrderItem item) { this.items.add(item); this.total = this.total.add(item.getSubtotal()); }
    
    /** @return The Order ID */
    public Long getId() { return id; }
    /** @param id The Order ID to set */
    public void setId(Long id) { this.id = id; }
    
    /** @return The current order status */
    public OrderStatus getStatus() { return status; }
    /** @param status The new order status */
    public void setStatus(OrderStatus status) { this.status = status; }
    
    /** @return The list of order items */
    public List<OrderItem> getItems() { return items; }
}
