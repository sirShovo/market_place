package application.domain.ports.in;

import application.domain.models.Buyer;
import application.domain.models.Cart;

/** Empties the buyer's active cart. */
public interface ClearCartUseCase {

    Cart clear(Buyer buyer);
}
