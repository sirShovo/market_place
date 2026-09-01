package application.domain.ports.in;

import application.domain.models.Order;
import application.domain.models.User;

/** Returns an order; a buyer only sees its own (spec RG03). */
public interface ConsultOrderUseCase {

    Order consult(User requester, Order probe);
}
