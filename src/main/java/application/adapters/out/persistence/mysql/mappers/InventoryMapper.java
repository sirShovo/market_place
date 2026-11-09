package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.InventoryItemJpaEntity;
import application.adapters.out.persistence.mysql.entities.InventoryMovementJpaEntity;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.valueobjects.InventoryItemCondition;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.StockQuantity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryMapper {

    private final ProductMapper productMapper;
    private final WarehouseMapper warehouseMapper;
    private final UserMapper userMapper;

    public InventoryItemJpaEntity toEntity(InventoryItem domain) {
        if (domain == null) {
            return null;
        }
        InventoryItemJpaEntity entity = new InventoryItemJpaEntity();
        entity.setId(domain.getId());
        entity.setProduct(productMapper.toEntity(domain.getProduct()));
        entity.setWarehouse(warehouseMapper.toEntity(domain.getWarehouse()));
        entity.setStock(domain.getStock() != null ? domain.getStock().value() : 0);
        entity.setItemCondition(domain.getCondition() != null ? domain.getCondition().getCode() : null);
        return entity;
    }

    public InventoryItem toDomain(InventoryItemJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        InventoryItem domain = new InventoryItem();
        domain.setId(entity.getId());
        domain.setProduct(productMapper.toDomain(entity.getProduct()));
        domain.setWarehouse(warehouseMapper.toDomain(entity.getWarehouse()));
        domain.setStock(StockQuantity.of(entity.getStock()));
        domain.setCondition(InventoryItemCondition.fromCode(entity.getItemCondition()));
        return domain;
    }

    public InventoryMovementJpaEntity toMovementEntity(InventoryMovement domain) {
        if (domain == null) {
            return null;
        }
        InventoryMovementJpaEntity entity = new InventoryMovementJpaEntity();
        entity.setId(domain.getId());
        entity.setInventoryItem(toEntity(domain.getInventoryItem()));
        entity.setType(domain.getType() != null ? domain.getType().getCode() : null);
        entity.setQuantity(domain.getQuantity());
        entity.setOccurredOn(domain.getOccurredOn());
        entity.setPerformedBy(userMapper.toEntity(domain.getPerformedBy()));
        return entity;
    }

    public InventoryMovement toMovementDomain(InventoryMovementJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        InventoryMovement domain = new InventoryMovement();
        domain.setId(entity.getId());
        domain.setInventoryItem(toDomain(entity.getInventoryItem()));
        domain.setType(InventoryMovementType.fromCode(entity.getType()));
        domain.setQuantity(entity.getQuantity());
        domain.setOccurredOn(entity.getOccurredOn());
        domain.setPerformedBy(userMapper.toDomain(entity.getPerformedBy()));
        return domain;
    }
}
