package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.BuyerJpaEntity;
import application.adapters.out.persistence.mysql.mappers.BuyerMapper;
import application.adapters.out.persistence.mysql.repositories.BuyerJpaRepository;
import application.domain.models.Buyer;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BuyerMySqlAdapter implements BuyerRepositoryPort {

    private final BuyerJpaRepository buyerJpaRepository;
    private final BuyerMapper buyerMapper;

    @Override
    public Buyer save(Buyer buyer) {
        BuyerJpaEntity saved = buyerJpaRepository.save(buyerMapper.toEntity(buyer));
        return buyerMapper.toDomain(saved);
    }

    @Override
    public Optional<Buyer> findByIdentification(Buyer buyer) {
        if (buyer == null || buyer.getIdentification() == null) {
            return Optional.empty();
        }
        return buyerJpaRepository.findById(buyer.getIdentification().value())
                .map(buyerMapper::toDomain);
    }

    @Override
    public boolean existsByIdentification(DocumentId identification) {
        if (identification == null) {
            return false;
        }
        return buyerJpaRepository.existsByIdentification(identification.value());
    }

    @Override
    public boolean existsByEmail(Email email) {
        if (email == null) {
            return false;
        }
        return buyerJpaRepository.existsByEmail(email.value());
    }

    @Override
    public void update(Buyer buyer) {
        buyerJpaRepository.save(buyerMapper.toEntity(buyer));
    }
}
