package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.SellerJpaEntity;
import application.domain.models.Seller;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.SellerStatus;
import application.domain.valueobjects.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SellerMapper {

    private final UserMapper userMapper;

    public SellerJpaEntity toEntity(Seller domain) {
        if (domain == null) {
            return null;
        }
        SellerJpaEntity entity = new SellerJpaEntity();
        entity.setIdentification(domain.getIdentification() != null ? domain.getIdentification().value() : null);
        entity.setFullName(domain.getFullName());
        entity.setEmail(domain.getEmail() != null ? domain.getEmail().value() : null);
        entity.setPhoneNumber(domain.getPhoneNumber());
        entity.setAddress(domain.getAddress());
        entity.setRole(domain.getRole() != null ? domain.getRole().getCode() : UserRole.SELLER.getCode());
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().getCode() : null);
        entity.setOnboardedBy(userMapper.toEntity(domain.getOnboardedBy()));
        return entity;
    }

    public Seller toDomain(SellerJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Seller domain = new Seller();
        if (entity.getIdentification() != null && !entity.getIdentification().isBlank()) {
            domain.setIdentification(new DocumentId(entity.getIdentification()));
        }
        domain.setFullName(entity.getFullName());
        if (entity.getEmail() != null && !entity.getEmail().isBlank()) {
            domain.setEmail(new Email(entity.getEmail()));
        }
        domain.setPhoneNumber(entity.getPhoneNumber());
        domain.setAddress(entity.getAddress());
        domain.setRole(UserRole.fromCode(entity.getRole()));
        domain.setStatus(SellerStatus.fromCode(entity.getStatus()));
        domain.setOnboardedBy(userMapper.toDomain(entity.getOnboardedBy()));
        return domain;
    }
}
