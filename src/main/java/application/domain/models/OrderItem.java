package application.domain.models;

import application.domain.valueobjects.Money;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A line within an {@link Order}. {@code unitPrice} is captured at checkout time.
 */
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    private Product product;
    private ProductVariant variant;
    private int quantity;
    private Money unitPrice;

    /** @return {@code unitPrice × quantity}. */
    public Money getSubtotal() {
        return unitPrice.multiply(quantity);
    }
}
