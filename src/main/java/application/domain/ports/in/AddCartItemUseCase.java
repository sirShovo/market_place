package application.domain.ports.in;

import application.domain.models.Buyer;
import application.domain.models.Cart;
import application.domain.models.CartItem;

/** Adds a line to the buyer's active cart. */
public interface AddCartItemUseCase {

    Cart addItem(Buyer buyer, CartItem item);
}
