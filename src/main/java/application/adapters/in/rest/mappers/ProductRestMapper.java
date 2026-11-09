package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.ProductVariantDto;
import application.adapters.in.rest.requests.PublishProductRequest;
import application.adapters.in.rest.requests.UpdateProductRequest;
import application.adapters.in.rest.responses.ProductResponse;
import application.domain.models.Category;
import application.domain.models.DigitalProduct;
import application.domain.models.PhysicalProduct;
import application.domain.models.Product;
import application.domain.models.ProductVariant;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.ProductType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProductRestMapper {

    public Product toDomain(PublishProductRequest request) {
        return toDomain(request, null);
    }

    public Product toDomain(PublishProductRequest request, User requester) {
        if (request == null) {
            return null;
        }
        ProductType productType = ProductType.fromCode(request.type());
        Product product = ProductType.DIGITAL.equals(productType)
                ? new DigitalProduct()
                : new PhysicalProduct();

        product.setIdentifier(request.identifier());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(Money.of(request.price()));

        String resolvedSellerId = (request.sellerIdentification() != null && !request.sellerIdentification().isBlank())
                ? request.sellerIdentification()
                : (requester != null && requester.getIdentification() != null
                        ? requester.getIdentification().value()
                        : null);
        if (resolvedSellerId != null && !resolvedSellerId.isBlank()) {
            Seller seller = new Seller();
            seller.setIdentification(new DocumentId(resolvedSellerId));
            product.setSeller(seller);
        }

        if (request.categoryId() != null || request.categoryName() != null) {
            Category category = new Category();
            category.setId(request.categoryId());
            category.setName(request.categoryName());
            product.setCategory(category);
        }

        product.setVariants(mapVariants(request.variants()));
        return product;
    }

    public Product toDomain(String identifier, UpdateProductRequest request) {
        if (request == null) {
            return null;
        }
        Product product = new PhysicalProduct();
        product.setIdentifier(identifier);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(Money.of(request.price()));

        if (request.categoryId() != null || request.categoryName() != null) {
            Category category = new Category();
            category.setId(request.categoryId());
            category.setName(request.categoryName());
            product.setCategory(category);
        }

        product.setVariants(mapVariants(request.variants()));
        return product;
    }

    public Product toStatusTarget(String identifier, String statusCode) {
        Product target = new PhysicalProduct();
        target.setIdentifier(identifier);
        target.setStatus(ProductStatus.fromCode(statusCode));
        return target;
    }

    public Product toProbe(String identifier) {
        Product probe = new PhysicalProduct();
        probe.setIdentifier(identifier);
        return probe;
    }

    public ProductResponse toResponse(Product domain) {
        if (domain == null) {
            return null;
        }
        List<ProductVariantDto> variants = domain.getVariants() != null
                ? domain.getVariants().stream()
                        .map(v -> new ProductVariantDto(v.getAttributeName(), v.getValue()))
                        .toList()
                : List.of();

        return new ProductResponse(
                domain.getIdentifier(),
                domain.getName(),
                domain.getDescription(),
                domain.getType() != null ? domain.getType().getCode() : null,
                domain.getStatus() != null ? domain.getStatus().getCode() : null,
                domain.getPrice() != null ? domain.getPrice().amount() : null,
                domain.getSeller() != null && domain.getSeller().getIdentification() != null
                        ? domain.getSeller().getIdentification().value()
                        : null,
                domain.getCategory() != null ? domain.getCategory().getId() : null,
                domain.getCategory() != null ? domain.getCategory().getName() : null,
                variants
        );
    }

    private List<ProductVariant> mapVariants(List<ProductVariantDto> dtos) {
        List<ProductVariant> variants = new ArrayList<>();
        if (dtos != null) {
            for (ProductVariantDto dto : dtos) {
                if (dto != null) {
                    ProductVariant variant = new ProductVariant();
                    variant.setAttributeName(dto.attributeName());
                    variant.setValue(dto.value());
                    variants.add(variant);
                }
            }
        }
        return variants;
    }
}
