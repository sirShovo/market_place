package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.CartJpaEntity;
import application.adapters.out.persistence.mysql.mappers.CartMapper;
import application.adapters.out.persistence.mysql.repositories.CartJpaRepository;
import application.domain.models.Buyer;
import application.domain.models.Cart;
import application.domain.ports.out.CartRepositoryPort;
import application.domain.valueobjects.CartStatus;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartMySqlAdapter implements CartRepositoryPort {

    private final CartJpaRepository cartJpaRepository;
    private final CartMapper cartMapper;

    @Override
    public Cart save(Cart cart) {
        CartJpaEntity saved = cartJpaRepository.save(cartMapper.toEntity(cart));
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
        cartJpaRepository.save(cartMapper.toEntity(cart));
    }
}
