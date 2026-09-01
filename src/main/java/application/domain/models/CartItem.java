package application.domain.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A line within a {@link Cart}.
 */
@Getter
@Setter
@NoArgsConstructor
public class CartItem {

    private Product product;
    private ProductVariant variant;
    private int quantity;
}
