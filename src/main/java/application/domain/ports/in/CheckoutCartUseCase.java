package application.domain.ports.in;

import application.domain.models.Buyer;
import application.domain.models.Order;

/** Converts the buyer's active cart into a {@code PENDING_PAYMENT} order. */
public interface CheckoutCartUseCase {

    Order checkout(Buyer buyer);
}
