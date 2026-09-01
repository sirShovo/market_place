package application.domain.services.cart;

import application.domain.exceptions.DomainException;
import application.domain.models.Buyer;
import application.domain.models.Cart;
import application.domain.models.CartItem;
import application.domain.ports.in.AddCartItemUseCase;
import application.domain.ports.out.CartRepositoryPort;
import application.domain.services.authorization.ValidateBuyerCanPurchaseService;
import application.domain.valueobjects.CartStatus;
import application.domain.valueobjects.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Adds a line to the buyer's single active cart, creating the cart on first use. Only
 * {@code PUBLISHED} products may be added.
 */
@Service
@RequiredArgsConstructor
public class AddCartItemService implements AddCartItemUseCase {

    private final CartRepositoryPort cartRepositoryPort;
    private final ValidateBuyerCanPurchaseService validateBuyerCanPurchaseService;

    @Override
    public Cart addItem(Buyer buyer, CartItem item) {
        validateBuyerCanPurchaseService.execute(buyer);
        if (item.getProduct() == null || !ProductStatus.PUBLISHED.equals(item.getProduct().getStatus())) {
            throw new DomainException("Only published products can be added to the cart.");
        }
        if (item.getQuantity() <= 0) {
            throw new DomainException("Cart item quantity must be positive.");
        }

        Cart cart = cartRepositoryPort.findActiveByBuyer(buyer).orElseGet(() -> {
            Cart fresh = new Cart();
            fresh.setBuyer(buyer);
            fresh.setStatus(CartStatus.ACTIVE);
            return cartRepositoryPort.save(fresh);
        });

        cart.getItems().add(item);
        cartRepositoryPort.update(cart);
        return cart;
    }
}
