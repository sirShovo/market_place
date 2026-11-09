package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.SellerJpaEntity;
import application.adapters.out.persistence.mysql.mappers.SellerMapper;
import application.adapters.out.persistence.mysql.mappers.WarehouseMapper;
import application.adapters.out.persistence.mysql.repositories.SellerJpaRepository;
import application.adapters.out.persistence.mysql.repositories.WarehouseJpaRepository;
import application.domain.models.Seller;
import application.domain.models.Warehouse;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SellerMySqlAdapter implements SellerRepositoryPort {

    private final SellerJpaRepository sellerJpaRepository;
    private final WarehouseJpaRepository warehouseJpaRepository;
    private final SellerMapper sellerMapper;
    private final WarehouseMapper warehouseMapper;

    @Override
    public Seller save(Seller seller) {
        SellerJpaEntity saved = sellerJpaRepository.save(sellerMapper.toEntity(seller));
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
        sellerJpaRepository.save(sellerMapper.toEntity(seller));
    }

    private Seller populateWarehouses(Seller seller) {
        if (seller != null && seller.getIdentification() != null) {
            List<Warehouse> warehouses = warehouseJpaRepository
                    .findByOwner_Identification(seller.getIdentification().value())
                    .stream()
                    .map(warehouseMapper::toDomain)
                    .toList();
            seller.setWarehouses(warehouses);
        }
        return seller;
    }
}
