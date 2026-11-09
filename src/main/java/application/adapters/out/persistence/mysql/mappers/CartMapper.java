package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.CartItemJpaEntity;
import application.adapters.out.persistence.mysql.entities.CartJpaEntity;
import application.domain.models.Cart;
import application.domain.models.CartItem;
import application.domain.models.ProductVariant;
import application.domain.valueobjects.CartStatus;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartMapper {

    private final BuyerMapper buyerMapper;
    private final ProductMapper productMapper;

    public CartJpaEntity toEntity(Cart domain) {
        if (domain == null) {
            return null;
        }
        CartJpaEntity entity = new CartJpaEntity();
        entity.setId(domain.getId());
        entity.setBuyer(buyerMapper.toEntity(domain.getBuyer()));
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().getCode() : null);

        List<CartItemJpaEntity> itemEntities = new ArrayList<>();
        if (domain.getItems() != null) {
            for (CartItem item : domain.getItems()) {
                if (item != null) {
                    CartItemJpaEntity itemEntity = new CartItemJpaEntity();
                    itemEntity.setCart(entity);
                    itemEntity.setProduct(productMapper.toEntity(item.getProduct()));
                    if (item.getVariant() != null) {
                        itemEntity.setVariantAttributeName(item.getVariant().getAttributeName());
                        itemEntity.setVariantValue(item.getVariant().getValue());
                    }
                    itemEntity.setQuantity(item.getQuantity());
                    itemEntities.add(itemEntity);
                }
            }
        }
        entity.setItems(itemEntities);
        return entity;
    }

    public Cart toDomain(CartJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Cart domain = new Cart();
        domain.setId(entity.getId());
        domain.setBuyer(buyerMapper.toDomain(entity.getBuyer()));
        domain.setStatus(CartStatus.fromCode(entity.getStatus()));

        List<CartItem> items = new ArrayList<>();
        if (entity.getItems() != null) {
            for (CartItemJpaEntity itemEntity : entity.getItems()) {
                if (itemEntity != null) {
                    CartItem item = new CartItem();
                    item.setProduct(productMapper.toDomain(itemEntity.getProduct()));
                    if (itemEntity.getVariantAttributeName() != null) {
                        ProductVariant variant = new ProductVariant();
                        variant.setAttributeName(itemEntity.getVariantAttributeName());
                        variant.setValue(itemEntity.getVariantValue());
                        item.setVariant(variant);
                    }
                    item.setQuantity(itemEntity.getQuantity());
                    items.add(item);
                }
            }
        }
        domain.setItems(items);
        return domain;
    }
}
