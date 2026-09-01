package application.domain.services.cart;

import application.domain.models.Buyer;
import application.domain.models.Cart;
import application.domain.ports.in.ConsultCartUseCase;
import application.domain.ports.out.CartRepositoryPort;
import application.domain.valueobjects.CartStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Returns the buyer's active cart, or an empty transient one when none exists yet.
 */
@Service
@RequiredArgsConstructor
public class ConsultCartService implements ConsultCartUseCase {

    private final CartRepositoryPort cartRepositoryPort;

    @Override
    public Cart consult(Buyer buyer) {
        return cartRepositoryPort.findActiveByBuyer(buyer).orElseGet(() -> {
            Cart empty = new Cart();
            empty.setBuyer(buyer);
            empty.setStatus(CartStatus.ACTIVE);
            return empty;
        });
    }
}
