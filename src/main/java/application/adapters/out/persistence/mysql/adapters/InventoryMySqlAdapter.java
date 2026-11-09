package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.InventoryItemJpaEntity;
import application.adapters.out.persistence.mysql.entities.InventoryMovementJpaEntity;
import application.adapters.out.persistence.mysql.entities.ProductJpaEntity;
import application.adapters.out.persistence.mysql.entities.WarehouseJpaEntity;
import application.adapters.out.persistence.mysql.mappers.InventoryMapper;
import application.adapters.out.persistence.mysql.repositories.InventoryItemJpaRepository;
import application.adapters.out.persistence.mysql.repositories.InventoryMovementJpaRepository;
import application.adapters.out.persistence.mysql.repositories.ProductJpaRepository;
import application.adapters.out.persistence.mysql.repositories.UserJpaRepository;
import application.adapters.out.persistence.mysql.repositories.WarehouseJpaRepository;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Product;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.valueobjects.ProductType;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InventoryMySqlAdapter implements InventoryRepositoryPort {

    private final InventoryItemJpaRepository inventoryItemJpaRepository;
    private final InventoryMovementJpaRepository inventoryMovementJpaRepository;
    private final InventoryMapper inventoryMapper;
    private final ProductJpaRepository productJpaRepository;
    private final WarehouseJpaRepository warehouseJpaRepository;
    private final UserJpaRepository userJpaRepository;

    public InventoryMySqlAdapter(
            InventoryItemJpaRepository inventoryItemJpaRepository,
            InventoryMovementJpaRepository inventoryMovementJpaRepository,
            InventoryMapper inventoryMapper) {
        this(inventoryItemJpaRepository, inventoryMovementJpaRepository, inventoryMapper, null, null, null);
    }

    @Autowired
    public InventoryMySqlAdapter(
            InventoryItemJpaRepository inventoryItemJpaRepository,
            InventoryMovementJpaRepository inventoryMovementJpaRepository,
            InventoryMapper inventoryMapper,
            ProductJpaRepository productJpaRepository,
            WarehouseJpaRepository warehouseJpaRepository,
            UserJpaRepository userJpaRepository) {
        this.inventoryItemJpaRepository = inventoryItemJpaRepository;
        this.inventoryMovementJpaRepository = inventoryMovementJpaRepository;
        this.inventoryMapper = inventoryMapper;
        this.productJpaRepository = productJpaRepository;
        this.warehouseJpaRepository = warehouseJpaRepository;
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public InventoryItem save(InventoryItem item) {
        InventoryItemJpaEntity entity = inventoryMapper.toEntity(item);
        resolveItemAssociations(entity);
        InventoryItemJpaEntity saved = inventoryItemJpaRepository.save(entity);
        if (saved != null && item != null) {
            item.setId(saved.getId());
        }
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
        resolveItemAssociations(entity);
        InventoryItemJpaEntity saved = inventoryItemJpaRepository.save(entity);
        if (saved != null && item != null && item.getId() == null) {
            item.setId(saved.getId());
        }
    }

    @Override
    public InventoryMovement saveMovement(InventoryMovement movement) {
        InventoryMovementJpaEntity entity = inventoryMapper.toMovementEntity(movement);
        if (entity != null) {
            if (entity.getInventoryItem() != null) {
                resolveItemAssociations(entity.getInventoryItem());
                if (entity.getInventoryItem().getId() == null
                        && entity.getInventoryItem().getProduct() != null
                        && entity.getInventoryItem().getWarehouse() != null) {
                    inventoryItemJpaRepository
                            .findByProduct_IdentifierAndWarehouse_Identifier(
                                    entity.getInventoryItem().getProduct().getIdentifier(),
                                    entity.getInventoryItem().getWarehouse().getIdentifier())
                            .ifPresent(entity::setInventoryItem);
                }
            }
            if (userJpaRepository != null
                    && entity.getPerformedBy() != null
                    && entity.getPerformedBy().getUserId() == null
                    && entity.getPerformedBy().getUsername() != null) {
                userJpaRepository.findByUsername(entity.getPerformedBy().getUsername())
                        .ifPresent(entity::setPerformedBy);
            }
        }
        InventoryMovementJpaEntity saved = inventoryMovementJpaRepository.save(entity);
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

    private void resolveItemAssociations(InventoryItemJpaEntity entity) {
        if (entity == null) {
            return;
        }
        if (productJpaRepository != null && entity.getProduct() != null && entity.getProduct().getIdentifier() != null) {
            Optional<ProductJpaEntity> managedProduct =
                    productJpaRepository.findById(entity.getProduct().getIdentifier());
            if (managedProduct.isPresent()) {
                if (ProductType.DIGITAL.getCode().equals(managedProduct.get().getProductType())) {
                    throw new DomainException("Digital products do not manage physical warehouse inventory.");
                }
                entity.setProduct(managedProduct.get());
            } else if (entity.getProduct().getName() == null) {
                throw new EntityNotFoundException("Product");
            }
        }
        if (warehouseJpaRepository != null
                && entity.getWarehouse() != null
                && entity.getWarehouse().getIdentifier() != null) {
            Optional<WarehouseJpaEntity> managedWarehouse =
                    warehouseJpaRepository.findById(entity.getWarehouse().getIdentifier());
            if (managedWarehouse.isPresent()) {
                entity.setWarehouse(managedWarehouse.get());
            } else if (entity.getWarehouse().getName() == null) {
                throw new EntityNotFoundException("Warehouse");
            }
        }
    }
}
