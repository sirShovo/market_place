package application.domain.ports.in;

import application.domain.models.Order;
import application.domain.models.User;

/** A {@code LOGISTICS_OPERATOR} dispatches a paid order ({@code PAID -> DISPATCHED}). */
public interface DispatchOrderUseCase {

    Order dispatch(User requester, Order order);
}
