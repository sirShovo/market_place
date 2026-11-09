package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.SellerJpaEntity;
import application.adapters.out.persistence.mysql.entities.WarehouseJpaEntity;
import application.adapters.out.persistence.mysql.mappers.WarehouseMapper;
import application.adapters.out.persistence.mysql.repositories.SellerJpaRepository;
import application.adapters.out.persistence.mysql.repositories.WarehouseJpaRepository;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Seller;
import application.domain.models.Warehouse;
import application.domain.ports.out.WarehouseRepositoryPort;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WarehouseMySqlAdapter implements WarehouseRepositoryPort {

    private final WarehouseJpaRepository warehouseJpaRepository;
    private final WarehouseMapper warehouseMapper;
    private final SellerJpaRepository sellerJpaRepository;

    public WarehouseMySqlAdapter(WarehouseJpaRepository warehouseJpaRepository, WarehouseMapper warehouseMapper) {
        this(warehouseJpaRepository, warehouseMapper, null);
    }

    @Autowired
    public WarehouseMySqlAdapter(
            WarehouseJpaRepository warehouseJpaRepository,
            WarehouseMapper warehouseMapper,
            SellerJpaRepository sellerJpaRepository) {
        this.warehouseJpaRepository = warehouseJpaRepository;
        this.warehouseMapper = warehouseMapper;
        this.sellerJpaRepository = sellerJpaRepository;
    }

    @Override
    public Warehouse save(Warehouse warehouse) {
        WarehouseJpaEntity entity = warehouseMapper.toEntity(warehouse);
        resolveOwner(entity);
        WarehouseJpaEntity saved = warehouseJpaRepository.save(entity);
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
        WarehouseJpaEntity entity = warehouseMapper.toEntity(warehouse);
        resolveOwner(entity);
        warehouseJpaRepository.save(entity);
    }

    private void resolveOwner(WarehouseJpaEntity entity) {
        if (entity != null && sellerJpaRepository != null
                && entity.getOwner() != null && entity.getOwner().getIdentification() != null) {
            Optional<SellerJpaEntity> managedSeller = sellerJpaRepository.findById(entity.getOwner().getIdentification());
            if (managedSeller.isPresent()) {
                entity.setOwner(managedSeller.get());
            } else if (entity.getOwner().getFullName() == null) {
                throw new EntityNotFoundException("Seller");
            }
        }
    }
}
