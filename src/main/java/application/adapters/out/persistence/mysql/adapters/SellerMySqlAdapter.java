package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.SellerJpaEntity;
import application.adapters.out.persistence.mysql.entities.UserJpaEntity;
import application.adapters.out.persistence.mysql.mappers.SellerMapper;
import application.adapters.out.persistence.mysql.mappers.WarehouseMapper;
import application.adapters.out.persistence.mysql.repositories.SellerJpaRepository;
import application.adapters.out.persistence.mysql.repositories.UserJpaRepository;
import application.adapters.out.persistence.mysql.repositories.WarehouseJpaRepository;
import application.domain.models.Seller;
import application.domain.models.Warehouse;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SellerMySqlAdapter implements SellerRepositoryPort {

    private final SellerJpaRepository sellerJpaRepository;
    private final WarehouseJpaRepository warehouseJpaRepository;
    private final SellerMapper sellerMapper;
    private final WarehouseMapper warehouseMapper;
    private final UserJpaRepository userJpaRepository;

    public SellerMySqlAdapter(
            SellerJpaRepository sellerJpaRepository,
            WarehouseJpaRepository warehouseJpaRepository,
            SellerMapper sellerMapper,
            WarehouseMapper warehouseMapper) {
        this(sellerJpaRepository, warehouseJpaRepository, sellerMapper, warehouseMapper, null);
    }

    @Autowired
    public SellerMySqlAdapter(
            SellerJpaRepository sellerJpaRepository,
            WarehouseJpaRepository warehouseJpaRepository,
            SellerMapper sellerMapper,
            WarehouseMapper warehouseMapper,
            UserJpaRepository userJpaRepository) {
        this.sellerJpaRepository = sellerJpaRepository;
        this.warehouseJpaRepository = warehouseJpaRepository;
        this.sellerMapper = sellerMapper;
        this.warehouseMapper = warehouseMapper;
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public Seller save(Seller seller) {
        SellerJpaEntity entity = sellerMapper.toEntity(seller);
        resolveOnboardedBy(entity);
        SellerJpaEntity saved = sellerJpaRepository.save(entity);
        Seller domain = sellerMapper.toDomain(saved);
        if (seller.getWarehouses() != null && !seller.getWarehouses().isEmpty()) {
            domain.setWarehouses(seller.getWarehouses());
        } else {
            populateWarehouses(domain);
        }
        return domain;
    }

    @Override
    public Optional<Seller> findByIdentification(Seller seller) {
        if (seller == null || seller.getIdentification() == null) {
            return Optional.empty();
        }
        return sellerJpaRepository.findById(seller.getIdentification().value())
                .map(sellerMapper::toDomain)
                .map(this::populateWarehouses);
    }

    @Override
    public boolean existsByIdentification(DocumentId identification) {
        if (identification == null) {
            return false;
        }
        return sellerJpaRepository.existsByIdentification(identification.value());
    }

    @Override
    public boolean existsByEmail(Email email) {
        if (email == null) {
            return false;
        }
        return sellerJpaRepository.existsByEmail(email.value());
    }

    @Override
    public List<Seller> findAll() {
        return sellerJpaRepository.findAll().stream()
                .map(sellerMapper::toDomain)
                .map(this::populateWarehouses)
                .toList();
    }

    @Override
    public void update(Seller seller) {
        SellerJpaEntity entity = sellerMapper.toEntity(seller);
        resolveOnboardedBy(entity);
        sellerJpaRepository.save(entity);
    }

    private void resolveOnboardedBy(SellerJpaEntity entity) {
        if (entity != null && userJpaRepository != null && entity.getOnboardedBy() != null) {
            if (entity.getOnboardedBy().getUserId() != null) {
                userJpaRepository.findById(entity.getOnboardedBy().getUserId())
                        .ifPresent(entity::setOnboardedBy);
            } else if (entity.getOnboardedBy().getUsername() != null) {
                userJpaRepository.findByUsername(entity.getOnboardedBy().getUsername())
                        .ifPresent(entity::setOnboardedBy);
            }
        }
    }

    private Seller populateWarehouses(Seller seller) {
        if (seller != null && seller.getIdentification() != null) {
            List<Warehouse> warehouses = new java.util.ArrayList<>(warehouseJpaRepository
                    .findByOwner_Identification(seller.getIdentification().value())
                    .stream()
                    .map(warehouseMapper::toDomain)
                    .toList());
            seller.setWarehouses(warehouses);
        }
        return seller;
    }
}
