package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.UserJpaEntity;
import application.adapters.out.persistence.mysql.mappers.UserMapper;
import application.adapters.out.persistence.mysql.repositories.UserJpaRepository;
import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMySqlAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        UserJpaEntity saved = userJpaRepository.save(userMapper.toEntity(user));
        return userMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findByUsername(User user) {
        if (user == null || user.getUsername() == null) {
            return Optional.empty();
        }
        return userJpaRepository.findByUsername(user.getUsername()).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByIdentification(User user) {
        if (user == null || user.getIdentification() == null) {
            return Optional.empty();
        }
        return userJpaRepository.findByIdentification(user.getIdentification().value())
                .map(userMapper::toDomain);
    }

    @Override
    public boolean existsByIdentification(DocumentId identification) {
        if (identification == null) {
            return false;
        }
        return userJpaRepository.existsByIdentification(identification.value());
    }

    @Override
    public boolean existsByEmail(Email email) {
        if (email == null) {
            return false;
        }
        return userJpaRepository.existsByEmail(email.value());
    }

    @Override
    public void update(User user) {
        UserJpaEntity entity = userMapper.toEntity(user);
        if (entity.getUserId() == null && entity.getUsername() != null) {
            userJpaRepository.findByUsername(entity.getUsername())
                    .ifPresent(existing -> entity.setUserId(existing.getUserId()));
        }
        userJpaRepository.save(entity);
    }
}
