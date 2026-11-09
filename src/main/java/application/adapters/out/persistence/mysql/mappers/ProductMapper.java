package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.ProductJpaEntity;
import application.adapters.out.persistence.mysql.entities.ProductVariantEmbeddable;
import application.domain.models.DigitalProduct;
import application.domain.models.PhysicalProduct;
import application.domain.models.Product;
import application.domain.models.ProductVariant;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.ProductType;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final SellerMapper sellerMapper;
    private final CategoryMapper categoryMapper;

    public ProductJpaEntity toEntity(Product domain) {
        if (domain == null) {
            return null;
        }
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setIdentifier(domain.getIdentifier());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setProductType(domain.getType() != null ? domain.getType().getCode() : ProductType.PHYSICAL.getCode());
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().getCode() : null);
        entity.setPrice(domain.getPrice() != null ? domain.getPrice().amount() : null);
        entity.setSeller(sellerMapper.toEntity(domain.getSeller()));
        entity.setCategory(categoryMapper.toEntity(domain.getCategory()));

        List<ProductVariantEmbeddable> variantEntities = new ArrayList<>();
        if (domain.getVariants() != null) {
            for (ProductVariant v : domain.getVariants()) {
                if (v != null) {
                    variantEntities.add(new ProductVariantEmbeddable(v.getAttributeName(), v.getValue()));
                }
            }
        }
        entity.setVariants(variantEntities);
        return entity;
    }

    public Product toDomain(ProductJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        ProductType productType = ProductType.fromCode(entity.getProductType());
        Product domain = ProductType.DIGITAL.equals(productType)
                ? new DigitalProduct()
                : new PhysicalProduct();

        domain.setIdentifier(entity.getIdentifier());
        domain.setName(entity.getName());
        domain.setDescription(entity.getDescription());
        domain.setStatus(ProductStatus.fromCode(entity.getStatus()));
        if (entity.getPrice() != null) {
            domain.setPrice(Money.of(entity.getPrice()));
        }
        domain.setSeller(sellerMapper.toDomain(entity.getSeller()));
        domain.setCategory(categoryMapper.toDomain(entity.getCategory()));

        List<ProductVariant> variants = new ArrayList<>();
        if (entity.getVariants() != null) {
            for (ProductVariantEmbeddable emb : entity.getVariants()) {
                if (emb != null) {
                    ProductVariant variant = new ProductVariant();
                    variant.setAttributeName(emb.getAttributeName());
                    variant.setValue(emb.getValue());
                    variants.add(variant);
                }
            }
        }
        domain.setVariants(variants);
        return domain;
    }
}
