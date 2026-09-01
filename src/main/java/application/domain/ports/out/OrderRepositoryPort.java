package application.domain.ports.out;

import application.domain.models.Buyer;
import application.domain.models.Order;
import java.util.List;
import java.util.Optional;

/** Persistence contract for {@link Order}. */
public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findByIdentifier(Order order);

    List<Order> findByBuyer(Buyer buyer);

    void update(Order order);
}
