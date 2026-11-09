package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.WarehouseJpaEntity;
import application.adapters.out.persistence.mysql.mappers.WarehouseMapper;
import application.adapters.out.persistence.mysql.repositories.WarehouseJpaRepository;
import application.domain.models.Seller;
import application.domain.models.Warehouse;
import application.domain.ports.out.WarehouseRepositoryPort;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WarehouseMySqlAdapter implements WarehouseRepositoryPort {

    private final WarehouseJpaRepository warehouseJpaRepository;
    private final WarehouseMapper warehouseMapper;

    @Override
    public Warehouse save(Warehouse warehouse) {
        WarehouseJpaEntity saved = warehouseJpaRepository.save(warehouseMapper.toEntity(warehouse));
        return warehouseMapper.toDomain(saved);
    }

    @Override
    public Optional<Warehouse> findByIdentifier(Warehouse warehouse) {
        if (warehouse == null || warehouse.getIdentifier() == null) {
            return Optional.empty();
        }
        return warehouseJpaRepository.findById(warehouse.getIdentifier())
                .map(warehouseMapper::toDomain);
    }

    @Override
    public List<Warehouse> findByOwner(Seller owner) {
        if (owner == null || owner.getIdentification() == null) {
            return List.of();
        }
        return warehouseJpaRepository.findByOwner_Identification(owner.getIdentification().value())
                .stream()
                .map(warehouseMapper::toDomain)
                .toList();
    }

    @Override
    public void update(Warehouse warehouse) {
        warehouseJpaRepository.save(warehouseMapper.toEntity(warehouse));
    }
}
