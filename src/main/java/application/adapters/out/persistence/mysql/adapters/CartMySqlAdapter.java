package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.CartItemJpaEntity;
import application.adapters.out.persistence.mysql.entities.CartJpaEntity;
import application.adapters.out.persistence.mysql.mappers.CartMapper;
import application.adapters.out.persistence.mysql.repositories.BuyerJpaRepository;
import application.adapters.out.persistence.mysql.repositories.CartJpaRepository;
import application.adapters.out.persistence.mysql.repositories.ProductJpaRepository;
import application.domain.models.Buyer;
import application.domain.models.Cart;
import application.domain.ports.out.CartRepositoryPort;
import application.domain.valueobjects.CartStatus;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CartMySqlAdapter implements CartRepositoryPort {

    private final CartJpaRepository cartJpaRepository;
    private final CartMapper cartMapper;
    private final BuyerJpaRepository buyerJpaRepository;
    private final ProductJpaRepository productJpaRepository;

    public CartMySqlAdapter(CartJpaRepository cartJpaRepository, CartMapper cartMapper) {
        this(cartJpaRepository, cartMapper, null, null);
    }

    @Autowired
    public CartMySqlAdapter(
            CartJpaRepository cartJpaRepository,
            CartMapper cartMapper,
            BuyerJpaRepository buyerJpaRepository,
            ProductJpaRepository productJpaRepository) {
        this.cartJpaRepository = cartJpaRepository;
        this.cartMapper = cartMapper;
        this.buyerJpaRepository = buyerJpaRepository;
        this.productJpaRepository = productJpaRepository;
    }

    @Override
    public Cart save(Cart cart) {
        CartJpaEntity entity = cartMapper.toEntity(cart);
        resolveRelationships(entity);
        CartJpaEntity saved = cartJpaRepository.save(entity);
        return cartMapper.toDomain(saved);
    }

    @Override
    public Optional<Cart> findActiveByBuyer(Buyer buyer) {
        if (buyer == null || buyer.getIdentification() == null) {
            return Optional.empty();
        }
        return cartJpaRepository
                .findByBuyer_IdentificationAndStatus(
                        buyer.getIdentification().value(), CartStatus.ACTIVE.getCode())
                .map(cartMapper::toDomain);
    }

    @Override
    public Optional<Cart> findById(Cart cart) {
        if (cart == null || cart.getId() == null) {
            return Optional.empty();
        }
        return cartJpaRepository.findById(cart.getId())
                .map(cartMapper::toDomain);
    }

    @Override
    public void update(Cart cart) {
        CartJpaEntity entity = cartMapper.toEntity(cart);
        resolveRelationships(entity);
        cartJpaRepository.save(entity);
    }

    private void resolveRelationships(CartJpaEntity entity) {
        if (entity == null) {
            return;
        }
        if (buyerJpaRepository != null && entity.getBuyer() != null && entity.getBuyer().getIdentification() != null) {
            buyerJpaRepository.findById(entity.getBuyer().getIdentification()).ifPresent(entity::setBuyer);
        }
        if (productJpaRepository != null && entity.getItems() != null) {
            for (CartItemJpaEntity item : entity.getItems()) {
                if (item != null && item.getProduct() != null && item.getProduct().getIdentifier() != null) {
                    productJpaRepository.findById(item.getProduct().getIdentifier()).ifPresent(item::setProduct);
                }
            }
        }
    }
}
