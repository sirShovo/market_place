package application.domain.services.cart;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Cart;
import application.domain.ports.in.ClearCartUseCase;
import application.domain.ports.out.CartRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Empties the buyer's active cart.
 */
@Service
@RequiredArgsConstructor
public class ClearCartService implements ClearCartUseCase {

    private final CartRepositoryPort cartRepositoryPort;

    @Override
    public Cart clear(Buyer buyer) {
        Cart cart = cartRepositoryPort.findActiveByBuyer(buyer)
                .orElseThrow(() -> new EntityNotFoundException("Active cart"));
        cart.getItems().clear();
        cartRepositoryPort.update(cart);
        return cart;
    }
}
