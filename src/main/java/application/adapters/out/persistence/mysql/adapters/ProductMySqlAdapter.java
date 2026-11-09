package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.CategoryJpaEntity;
import application.adapters.out.persistence.mysql.entities.ProductJpaEntity;
import application.adapters.out.persistence.mysql.entities.SellerJpaEntity;
import application.adapters.out.persistence.mysql.mappers.ProductMapper;
import application.adapters.out.persistence.mysql.repositories.CategoryJpaRepository;
import application.adapters.out.persistence.mysql.repositories.ProductJpaRepository;
import application.adapters.out.persistence.mysql.repositories.SellerJpaRepository;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.valueobjects.ProductStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductMySqlAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository productJpaRepository;
    private final ProductMapper productMapper;
    private final CategoryJpaRepository categoryJpaRepository;
    private final SellerJpaRepository sellerJpaRepository;

    public ProductMySqlAdapter(ProductJpaRepository productJpaRepository, ProductMapper productMapper) {
        this(productJpaRepository, productMapper, null, null);
    }

    @Autowired
    public ProductMySqlAdapter(
            ProductJpaRepository productJpaRepository,
            ProductMapper productMapper,
            CategoryJpaRepository categoryJpaRepository,
            SellerJpaRepository sellerJpaRepository) {
        this.productJpaRepository = productJpaRepository;
        this.productMapper = productMapper;
        this.categoryJpaRepository = categoryJpaRepository;
        this.sellerJpaRepository = sellerJpaRepository;
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = productMapper.toEntity(product);
        resolveAssociations(entity);
        ProductJpaEntity saved = productJpaRepository.save(entity);
        return productMapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findByIdentifier(Product product) {
        if (product == null || product.getIdentifier() == null) {
            return Optional.empty();
        }
        return productJpaRepository.findById(product.getIdentifier())
                .map(productMapper::toDomain);
    }

    @Override
    public List<Product> findBySeller(Seller seller) {
        if (seller == null || seller.getIdentification() == null) {
            return List.of();
        }
        return productJpaRepository.findBySeller_Identification(seller.getIdentification().value())
                .stream()
                .map(productMapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findPublished() {
        return productJpaRepository.findByStatus(ProductStatus.PUBLISHED.getCode())
                .stream()
                .map(productMapper::toDomain)
                .toList();
    }

    @Override
    public void update(Product product) {
        ProductJpaEntity entity = productMapper.toEntity(product);
        resolveAssociations(entity);
        productJpaRepository.save(entity);
    }

    private void resolveAssociations(ProductJpaEntity entity) {
        if (entity == null) {
            return;
        }
        if (sellerJpaRepository != null && entity.getSeller() != null && entity.getSeller().getIdentification() != null) {
            Optional<SellerJpaEntity> managedSeller = sellerJpaRepository.findById(entity.getSeller().getIdentification());
            if (managedSeller.isPresent()) {
                entity.setSeller(managedSeller.get());
            } else if (entity.getSeller().getFullName() == null) {
                throw new EntityNotFoundException("Seller");
            }
        }
        if (categoryJpaRepository != null && entity.getCategory() != null) {
            CategoryJpaEntity cat = entity.getCategory();
            if (cat.getId() != null) {
                categoryJpaRepository.findById(cat.getId()).ifPresent(entity::setCategory);
            } else if (cat.getName() != null && !cat.getName().isBlank()) {
                CategoryJpaEntity resolved = categoryJpaRepository.findByName(cat.getName())
                        .orElseGet(() -> categoryJpaRepository.save(cat));
                entity.setCategory(resolved);
            } else {
                entity.setCategory(null);
            }
        }
    }
}
