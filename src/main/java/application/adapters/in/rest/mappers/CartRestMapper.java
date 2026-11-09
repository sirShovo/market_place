package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.CartItemRequest;
import application.adapters.in.rest.responses.CartItemResponse;
import application.adapters.in.rest.responses.CartResponse;
import application.domain.models.Cart;
import application.domain.models.CartItem;
import application.domain.models.PhysicalProduct;
import application.domain.models.Product;
import application.domain.models.ProductVariant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CartRestMapper {

    public CartItem toDomain(CartItemRequest request) {
        if (request == null) {
            return null;
        }
        Product product = new PhysicalProduct();
        product.setIdentifier(request.productIdentifier());

        CartItem item = new CartItem();
        item.setProduct(product);
        if (request.variantAttributeName() != null && !request.variantAttributeName().isBlank()) {
            ProductVariant variant = new ProductVariant();
            variant.setAttributeName(request.variantAttributeName());
            variant.setValue(request.variantValue());
            item.setVariant(variant);
        }
        item.setQuantity(request.quantity());
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
