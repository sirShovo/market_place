package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.UserJpaEntity;
import application.domain.models.User;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.UserStatus;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserJpaEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        UserJpaEntity entity = new UserJpaEntity();
        entity.setUserId(domain.getUserId());
        entity.setUsername(domain.getUsername());
        entity.setPassword(domain.getPassword());
        entity.setIdentification(domain.getIdentification() != null ? domain.getIdentification().value() : null);
        entity.setFullName(domain.getFullName());
        entity.setEmail(domain.getEmail() != null ? domain.getEmail().value() : null);
        entity.setPhoneNumber(domain.getPhoneNumber());
        entity.setAddress(domain.getAddress());
        entity.setRole(domain.getRole() != null ? domain.getRole().getCode() : null);
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().getCode() : null);
        return entity;
    }

    public User toDomain(UserJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        User domain = new User();
        domain.setUserId(entity.getUserId());
        domain.setUsername(entity.getUsername());
        domain.setPassword(entity.getPassword());
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
        domain.setStatus(UserStatus.fromCode(entity.getStatus()));
        return domain;
    }
}
