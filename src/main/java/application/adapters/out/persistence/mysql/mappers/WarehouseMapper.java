package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.WarehouseJpaEntity;
import application.domain.models.Warehouse;
import application.domain.valueobjects.WarehouseType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WarehouseMapper {

    private final SellerMapper sellerMapper;

    public WarehouseJpaEntity toEntity(Warehouse domain) {
        if (domain == null) {
            return null;
        }
        WarehouseJpaEntity entity = new WarehouseJpaEntity();
        entity.setIdentifier(domain.getIdentifier());
        entity.setName(domain.getName());
        entity.setType(domain.getType() != null ? domain.getType().getCode() : null);
        entity.setOwner(sellerMapper.toEntity(domain.getOwner()));
        entity.setLocation(domain.getLocation());
        return entity;
    }

    public Warehouse toDomain(WarehouseJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Warehouse domain = new Warehouse();
        domain.setIdentifier(entity.getIdentifier());
        domain.setName(entity.getName());
        domain.setType(WarehouseType.fromCode(entity.getType()));
        domain.setOwner(sellerMapper.toDomain(entity.getOwner()));
        domain.setLocation(entity.getLocation());
        return domain;
    }
}
