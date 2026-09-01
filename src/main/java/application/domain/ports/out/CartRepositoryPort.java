package application.domain.ports.out;

import application.domain.models.Buyer;
import application.domain.models.Cart;
import java.util.Optional;

/** Persistence contract for {@link Cart}. */
public interface CartRepositoryPort {

    Cart save(Cart cart);

    Optional<Cart> findActiveByBuyer(Buyer buyer);

    Optional<Cart> findById(Cart cart);

    void update(Cart cart);
}
