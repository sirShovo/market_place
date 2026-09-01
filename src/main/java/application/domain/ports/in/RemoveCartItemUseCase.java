package application.domain.ports.in;

import application.domain.models.Buyer;
import application.domain.models.Cart;
import application.domain.models.CartItem;

/** Removes a line from the buyer's active cart. */
public interface RemoveCartItemUseCase {

    Cart removeItem(Buyer buyer, CartItem item);
}
