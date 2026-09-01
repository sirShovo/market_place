package application.domain.ports.in;

import application.domain.models.Order;
import application.domain.models.User;

/** Confirms delivery of a dispatched order ({@code DISPATCHED -> DELIVERED}, finalized). */
public interface ConfirmDeliveryUseCase {

    Order confirmDelivery(User requester, Order order);
}
