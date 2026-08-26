package domain.repository;
import domain.model.aggregate.Order;
import java.util.Optional;
public interface OrderRepository { Order save(Order order); Optional<Order> findById(Long id); }
