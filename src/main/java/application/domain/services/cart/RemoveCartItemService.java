package application.domain.services.cart;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Cart;
import application.domain.models.CartItem;
import application.domain.ports.in.RemoveCartItemUseCase;
import application.domain.ports.out.CartRepositoryPort;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Removes the line matching the given product (and variant) from the buyer's active
 * cart.
 */
@Service
@RequiredArgsConstructor
public class RemoveCartItemService implements RemoveCartItemUseCase {

    private final CartRepositoryPort cartRepositoryPort;

    @Override
    public Cart removeItem(Buyer buyer, CartItem item) {
        Cart cart = cartRepositoryPort.findActiveByBuyer(buyer)
                .orElseThrow(() -> new EntityNotFoundException("Active cart"));

        cart.getItems().removeIf(line ->
                sameProduct(line, item) && Objects.equals(line.getVariant(), item.getVariant()));
        cartRepositoryPort.update(cart);
        return cart;
    }

    private boolean sameProduct(CartItem a, CartItem b) {
        return a.getProduct() != null && b.getProduct() != null
                && Objects.equals(a.getProduct().getIdentifier(), b.getProduct().getIdentifier());
    }
}
