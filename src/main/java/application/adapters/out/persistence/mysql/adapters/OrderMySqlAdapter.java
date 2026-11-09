package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.OrderJpaEntity;
import application.adapters.out.persistence.mysql.mappers.OrderMapper;
import application.adapters.out.persistence.mysql.repositories.OrderJpaRepository;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.valueobjects.OrderStatus;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMySqlAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderMapper orderMapper;

    @Override
    public Order save(Order order) {
        OrderJpaEntity saved = orderJpaRepository.save(orderMapper.toEntity(order));
        return orderMapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findByIdentifier(Order order) {
        if (order == null || order.getIdentifier() == null) {
            return Optional.empty();
        }
        return orderJpaRepository.findById(order.getIdentifier())
                .map(orderMapper::toDomain);
    }

    @Override
    public List<Order> findByBuyer(Buyer buyer) {
        if (buyer == null || buyer.getIdentification() == null) {
            return List.of();
        }
        return orderJpaRepository.findByBuyer_Identification(buyer.getIdentification().value())
                .stream()
                .map(orderMapper::toDomain)
                .toList();
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        if (status == null) {
            return List.of();
        }
        return orderJpaRepository.findByStatus(status.getCode())
                .stream()
                .map(orderMapper::toDomain)
                .toList();
    }

    @Override
    public void update(Order order) {
        orderJpaRepository.save(orderMapper.toEntity(order));
    }
}
