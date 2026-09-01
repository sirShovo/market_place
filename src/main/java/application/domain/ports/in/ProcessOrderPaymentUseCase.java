package application.domain.ports.in;

import application.domain.models.Buyer;
import application.domain.models.Order;

/** Processes payment for an order; on rejection the buyer may retry. */
public interface ProcessOrderPaymentUseCase {

    Order pay(Buyer buyer, Order order);
}
