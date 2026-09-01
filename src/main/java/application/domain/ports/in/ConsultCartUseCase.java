package application.domain.ports.in;

import application.domain.models.Buyer;
import application.domain.models.Cart;

/** Returns the buyer's active cart. */
public interface ConsultCartUseCase {

    Cart consult(Buyer buyer);
}
