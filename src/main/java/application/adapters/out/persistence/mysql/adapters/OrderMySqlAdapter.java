package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.OrderItemJpaEntity;
import application.adapters.out.persistence.mysql.entities.OrderJpaEntity;
import application.adapters.out.persistence.mysql.mappers.OrderMapper;
import application.adapters.out.persistence.mysql.repositories.BuyerJpaRepository;
import application.adapters.out.persistence.mysql.repositories.OrderJpaRepository;
import application.adapters.out.persistence.mysql.repositories.ProductJpaRepository;
import application.adapters.out.persistence.mysql.repositories.WarehouseJpaRepository;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.valueobjects.OrderStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderMySqlAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderMapper orderMapper;
    private final BuyerJpaRepository buyerJpaRepository;
    private final ProductJpaRepository productJpaRepository;
    private final WarehouseJpaRepository warehouseJpaRepository;

    public OrderMySqlAdapter(OrderJpaRepository orderJpaRepository, OrderMapper orderMapper) {
        this(orderJpaRepository, orderMapper, null, null, null);
    }

    @Autowired
    public OrderMySqlAdapter(
            OrderJpaRepository orderJpaRepository,
            OrderMapper orderMapper,
            BuyerJpaRepository buyerJpaRepository,
            ProductJpaRepository productJpaRepository,
            WarehouseJpaRepository warehouseJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
        this.orderMapper = orderMapper;
        this.buyerJpaRepository = buyerJpaRepository;
        this.productJpaRepository = productJpaRepository;
        this.warehouseJpaRepository = warehouseJpaRepository;
    }

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = orderMapper.toEntity(order);
        resolveRelationships(entity);
        OrderJpaEntity saved = orderJpaRepository.save(entity);
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
        OrderJpaEntity entity = orderMapper.toEntity(order);
        resolveRelationships(entity);
        orderJpaRepository.save(entity);
    }

    private void resolveRelationships(OrderJpaEntity entity) {
        if (entity == null) {
            return;
        }
        if (buyerJpaRepository != null && entity.getBuyer() != null && entity.getBuyer().getIdentification() != null) {
            buyerJpaRepository.findById(entity.getBuyer().getIdentification()).ifPresent(entity::setBuyer);
        }
        if (entity.getItems() != null) {
            for (OrderItemJpaEntity item : entity.getItems()) {
                if (item != null) {
                    if (productJpaRepository != null && item.getProduct() != null && item.getProduct().getIdentifier() != null) {
                        productJpaRepository.findById(item.getProduct().getIdentifier()).ifPresent(item::setProduct);
                    }
                    if (warehouseJpaRepository != null && item.getWarehouse() != null && item.getWarehouse().getIdentifier() != null) {
                        warehouseJpaRepository.findById(item.getWarehouse().getIdentifier()).ifPresent(item::setWarehouse);
                    }
                }
            }
        }
    }
}
