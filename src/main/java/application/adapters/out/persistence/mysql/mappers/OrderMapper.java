package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.OrderItemJpaEntity;
import application.adapters.out.persistence.mysql.entities.OrderJpaEntity;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.ProductVariant;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderStatus;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final BuyerMapper buyerMapper;
    private final ProductMapper productMapper;
    private final WarehouseMapper warehouseMapper;

    public OrderJpaEntity toEntity(Order domain) {
        if (domain == null) {
            return null;
        }
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setIdentifier(domain.getIdentifier());
        entity.setBuyer(buyerMapper.toEntity(domain.getBuyer()));
        entity.setTotal(domain.getTotal() != null ? domain.getTotal().amount() : Money.zero().amount());
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().getCode() : OrderStatus.CART.getCode());
        entity.setCreatedAt(domain.getCreatedAt());

        List<OrderItemJpaEntity> itemEntities = new ArrayList<>();
        if (domain.getItems() != null) {
            for (OrderItem item : domain.getItems()) {
                if (item != null) {
                    OrderItemJpaEntity itemEntity = new OrderItemJpaEntity();
                    itemEntity.setOrder(entity);
                    itemEntity.setProduct(productMapper.toEntity(item.getProduct()));
                    if (item.getVariant() != null) {
                        itemEntity.setVariantAttributeName(item.getVariant().getAttributeName());
                        itemEntity.setVariantValue(item.getVariant().getValue());
                    }
                    itemEntity.setQuantity(item.getQuantity());
                    itemEntity.setUnitPrice(item.getUnitPrice() != null ? item.getUnitPrice().amount() : null);
                    itemEntity.setWarehouse(warehouseMapper.toEntity(item.getWarehouse()));
                    itemEntities.add(itemEntity);
                }
            }
        }
        entity.setItems(itemEntities);
        return entity;
    }

    public Order toDomain(OrderJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Order domain = new Order();
        domain.setIdentifier(entity.getIdentifier());
        domain.setBuyer(buyerMapper.toDomain(entity.getBuyer()));
        if (entity.getTotal() != null) {
            domain.setTotal(Money.of(entity.getTotal()));
        }
        domain.setCreatedAt(entity.getCreatedAt());

        List<OrderItem> items = new ArrayList<>();
        if (entity.getItems() != null) {
            for (OrderItemJpaEntity itemEntity : entity.getItems()) {
                if (itemEntity != null) {
                    OrderItem item = new OrderItem();
                    item.setProduct(productMapper.toDomain(itemEntity.getProduct()));
                    if (itemEntity.getVariantAttributeName() != null) {
                        ProductVariant variant = new ProductVariant();
                        variant.setAttributeName(itemEntity.getVariantAttributeName());
                        variant.setValue(itemEntity.getVariantValue());
                        item.setVariant(variant);
                    }
                    item.setQuantity(itemEntity.getQuantity());
                    if (itemEntity.getUnitPrice() != null) {
                        item.setUnitPrice(Money.of(itemEntity.getUnitPrice()));
                    }
                    item.setWarehouse(warehouseMapper.toDomain(itemEntity.getWarehouse()));
                    items.add(item);
                }
            }
        }
        domain.setItems(items);
        if (entity.getStatus() != null) {
            domain.setStatus(OrderStatus.fromCode(entity.getStatus()));
        }
        return domain;
    }
}
