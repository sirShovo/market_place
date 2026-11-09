package application.domain.ports.in;

import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.User;
import java.util.List;

/**
 * Lists all orders placed by a buyer; a {@code BUYER} only sees its own orders
 * (spec RG03).
 */
public interface ConsultBuyerOrdersUseCase {

    List<Order> consultByBuyer(User requester, Buyer buyer);
}
