package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.InventoryItemJpaEntity;
import application.adapters.out.persistence.mysql.entities.InventoryMovementJpaEntity;
import application.adapters.out.persistence.mysql.mappers.InventoryMapper;
import application.adapters.out.persistence.mysql.repositories.InventoryItemJpaRepository;
import application.adapters.out.persistence.mysql.repositories.InventoryMovementJpaRepository;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Product;
import application.domain.ports.out.InventoryRepositoryPort;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryMySqlAdapter implements InventoryRepositoryPort {

    private final InventoryItemJpaRepository inventoryItemJpaRepository;
    private final InventoryMovementJpaRepository inventoryMovementJpaRepository;
    private final InventoryMapper inventoryMapper;

    @Override
    public InventoryItem save(InventoryItem item) {
        InventoryItemJpaEntity saved = inventoryItemJpaRepository.save(inventoryMapper.toEntity(item));
        return inventoryMapper.toDomain(saved);
    }

    @Override
    public Optional<InventoryItem> findByProductAndWarehouse(InventoryItem probe) {
        if (probe == null || probe.getProduct() == null || probe.getWarehouse() == null) {
            return Optional.empty();
        }
        String productIdentifier = probe.getProduct().getIdentifier();
        String warehouseIdentifier = probe.getWarehouse().getIdentifier();
        if (productIdentifier == null || warehouseIdentifier == null) {
            return Optional.empty();
        }
        return inventoryItemJpaRepository
                .findByProduct_IdentifierAndWarehouse_Identifier(productIdentifier, warehouseIdentifier)
                .map(inventoryMapper::toDomain);
    }

    @Override
    public List<InventoryItem> findByProduct(Product product) {
        if (product == null || product.getIdentifier() == null) {
            return List.of();
        }
        return inventoryItemJpaRepository.findByProduct_Identifier(product.getIdentifier())
                .stream()
                .map(inventoryMapper::toDomain)
                .toList();
    }

    @Override
    public void update(InventoryItem item) {
        InventoryItemJpaEntity entity = inventoryMapper.toEntity(item);
        if (entity.getId() == null
                && item.getProduct() != null
                && item.getWarehouse() != null) {
            inventoryItemJpaRepository
                    .findByProduct_IdentifierAndWarehouse_Identifier(
                            item.getProduct().getIdentifier(),
                            item.getWarehouse().getIdentifier())
                    .ifPresent(existing -> entity.setId(existing.getId()));
        }
        inventoryItemJpaRepository.save(entity);
    }

    @Override
    public InventoryMovement saveMovement(InventoryMovement movement) {
        InventoryMovementJpaEntity saved = inventoryMovementJpaRepository.save(
                inventoryMapper.toMovementEntity(movement));
        return inventoryMapper.toMovementDomain(saved);
    }

    @Override
    public List<InventoryMovement> findMovements(InventoryItem item) {
        if (item == null || item.getId() == null) {
            return List.of();
        }
        return inventoryMovementJpaRepository.findByInventoryItem_Id(item.getId())
                .stream()
                .map(inventoryMapper::toMovementDomain)
                .toList();
    }
}
