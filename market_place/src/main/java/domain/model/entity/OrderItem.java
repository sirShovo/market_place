package domain.model.entity;
import domain.model.valueobject.Money;
public class OrderItem {
    private Long id;
    private Long productId;
    private int quantity;
    private Money unitPrice;
    public OrderItem(Long productId, int quantity, Money unitPrice) {
        this.productId = productId; this.quantity = quantity; this.unitPrice = unitPrice;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public Money getSubtotal() { return unitPrice.multiply(quantity); }
}
