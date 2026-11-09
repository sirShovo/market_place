package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.CartItemRequest;
import application.adapters.in.rest.requests.RemoveCartItemRequest;
import application.adapters.in.rest.responses.CartItemResponse;
import application.adapters.in.rest.responses.CartResponse;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Cart;
import application.domain.models.CartItem;
import application.domain.models.PhysicalProduct;
import application.domain.models.Product;
import application.domain.models.ProductVariant;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.valueobjects.ProductStatus;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CartRestMapper {

    private final ProductRepositoryPort productRepositoryPort;

    public CartRestMapper() {
        this.productRepositoryPort = null;
    }

    @Autowired
    public CartRestMapper(ProductRepositoryPort productRepositoryPort) {
        this.productRepositoryPort = productRepositoryPort;
    }

    public CartItem toDomain(CartItemRequest request) {
        if (request == null) {
            return null;
        }
        Product probe = new PhysicalProduct();
        probe.setIdentifier(request.productIdentifier());
        probe.setStatus(ProductStatus.PUBLISHED);

        Product resolvedProduct = productRepositoryPort != null
                ? productRepositoryPort.findByIdentifier(probe)
                        .orElseThrow(() -> new EntityNotFoundException("Product"))
                : probe;

        CartItem item = new CartItem();
        item.setProduct(resolvedProduct);
        if (request.variantAttributeName() != null && !request.variantAttributeName().isBlank()) {
            ProductVariant variant = new ProductVariant();
            variant.setAttributeName(request.variantAttributeName());
            variant.setValue(request.variantValue());
            item.setVariant(variant);
        }
        item.setQuantity(request.quantity());
        return item;
    }

    public CartItem toRemoveDomain(RemoveCartItemRequest request) {
        if (request == null) {
            return null;
        }
        return toRemoveDomain(
                request.productIdentifier(),
                request.variantAttributeName(),
                request.variantValue());
    }

    public CartItem toRemoveDomain(String productIdentifier, String variantAttributeName, String variantValue) {
        Product probe = new PhysicalProduct();
        probe.setIdentifier(productIdentifier);

        CartItem item = new CartItem();
        item.setProduct(probe);
        if (variantAttributeName != null && !variantAttributeName.isBlank()) {
            ProductVariant variant = new ProductVariant();
            variant.setAttributeName(variantAttributeName);
            variant.setValue(variantValue);
            item.setVariant(variant);
        }
        item.setQuantity(1);
        return item;
    }

    public CartResponse toResponse(Cart domain) {
        if (domain == null) {
            return null;
        }
        List<CartItemResponse> items = domain.getItems() != null
                ? domain.getItems().stream().map(this::toItemResponse).toList()
                : List.of();

        return new CartResponse(
                domain.getId(),
                domain.getBuyer() != null && domain.getBuyer().getIdentification() != null
                        ? domain.getBuyer().getIdentification().value()
                        : null,
                domain.getStatus() != null ? domain.getStatus().getCode() : null,
                items
        );
    }

    private CartItemResponse toItemResponse(CartItem item) {
        return new CartItemResponse(
                item.getProduct() != null ? item.getProduct().getIdentifier() : null,
                item.getProduct() != null ? item.getProduct().getName() : null,
                item.getVariant() != null ? item.getVariant().getAttributeName() : null,
                item.getVariant() != null ? item.getVariant().getValue() : null,
                item.getQuantity()
        );
    }
}
