package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.ProductJpaEntity;
import application.adapters.out.persistence.mysql.mappers.ProductMapper;
import application.adapters.out.persistence.mysql.repositories.ProductJpaRepository;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.valueobjects.ProductStatus;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMySqlAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository productJpaRepository;
    private final ProductMapper productMapper;

    @Override
    public Product save(Product product) {
        ProductJpaEntity saved = productJpaRepository.save(productMapper.toEntity(product));
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
        productJpaRepository.save(productMapper.toEntity(product));
    }
}
