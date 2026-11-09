package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.BuyerJpaEntity;
import application.domain.models.Buyer;
import application.domain.valueobjects.BuyerCommercialStatus;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.UserRole;
import java.util.ArrayList;
import org.springframework.stereotype.Component;

@Component
public class BuyerMapper {

    public BuyerJpaEntity toEntity(Buyer domain) {
        if (domain == null) {
            return null;
        }
        BuyerJpaEntity entity = new BuyerJpaEntity();
        entity.setIdentification(domain.getIdentification() != null ? domain.getIdentification().value() : null);
        entity.setFullName(domain.getFullName());
        entity.setEmail(domain.getEmail() != null ? domain.getEmail().value() : null);
        entity.setPhoneNumber(domain.getPhoneNumber());
        entity.setAddress(domain.getAddress());
        entity.setRole(domain.getRole() != null ? domain.getRole().getCode() : UserRole.BUYER.getCode());
        entity.setCommercialStatus(
                domain.getCommercialStatus() != null ? domain.getCommercialStatus().getCode() : null);
        entity.setMainAddress(domain.getMainAddress());
        entity.setAdditionalAddresses(
                domain.getAdditionalAddresses() != null
                        ? new ArrayList<>(domain.getAdditionalAddresses())
                        : new ArrayList<>());
        return entity;
    }

    public Buyer toDomain(BuyerJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Buyer domain = new Buyer();
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
        domain.setCommercialStatus(BuyerCommercialStatus.fromCode(entity.getCommercialStatus()));
        domain.setMainAddress(entity.getMainAddress());
        domain.setAdditionalAddresses(
                entity.getAdditionalAddresses() != null
                        ? new ArrayList<>(entity.getAdditionalAddresses())
                        : new ArrayList<>());
        return domain;
    }
}
