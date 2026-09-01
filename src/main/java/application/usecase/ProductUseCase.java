package application.usecase;

import application.dto.ProductDto;
import application.port.input.ProductInputPort;
import application.domain.model.aggregate.Product;
import application.domain.model.valueobject.Money;
import application.domain.model.valueobject.ProductType;
import application.domain.repository.ProductRepository;
import java.math.BigDecimal;

/**
 * Use Case implementation for Product operations.
 */
public class ProductUseCase implements ProductInputPort {

    private final ProductRepository productRepository;

    public ProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductDto publishProduct(String name, ProductType type, BigDecimal price, Long sellerId) {
        Product product = new Product(name, type, Money.of(price), sellerId);
        Product savedProduct = productRepository.save(product);

        return new ProductDto(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getType(),
                savedProduct.getPrice().getAmount(),
                savedProduct.getSellerId()
        );
    }
}
