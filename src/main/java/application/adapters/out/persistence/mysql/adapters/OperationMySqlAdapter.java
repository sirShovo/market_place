package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.OperationJpaEntity;
import application.adapters.out.persistence.mysql.mappers.OperationMapper;
import application.adapters.out.persistence.mysql.repositories.OperationJpaRepository;
import application.adapters.out.persistence.mysql.repositories.UserJpaRepository;
import application.domain.models.AuditableEntity;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.out.OperationRepositoryPort;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OperationMySqlAdapter implements OperationRepositoryPort {

    private final OperationJpaRepository operationJpaRepository;
    private final OperationMapper operationMapper;
    private final UserJpaRepository userJpaRepository;

    public OperationMySqlAdapter(OperationJpaRepository operationJpaRepository, OperationMapper operationMapper) {
        this(operationJpaRepository, operationMapper, null);
    }

    @Autowired
    public OperationMySqlAdapter(
            OperationJpaRepository operationJpaRepository,
            OperationMapper operationMapper,
            UserJpaRepository userJpaRepository) {
        this.operationJpaRepository = operationJpaRepository;
        this.operationMapper = operationMapper;
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public Operation save(Operation operation) {
        OperationJpaEntity entity = operationMapper.toEntity(operation);
        if (entity != null
                && userJpaRepository != null
                && entity.getPerformedBy() != null
                && entity.getPerformedBy().getUserId() == null
                && entity.getPerformedBy().getUsername() != null) {
            userJpaRepository.findByUsername(entity.getPerformedBy().getUsername())
                    .ifPresent(entity::setPerformedBy);
        }
        OperationJpaEntity saved = operationJpaRepository.save(entity);
        return saved != null ? operationMapper.toDomain(saved) : operation;
    }

    @Override
    public List<Operation> findByUser(User user) {
        if (user == null || user.getUsername() == null) {
            return List.of();
        }
        return operationJpaRepository.findByPerformedBy_Username(user.getUsername())
                .stream()
                .map(operationMapper::toDomain)
                .toList();
    }

    @Override
    public List<Operation> findByEntity(AuditableEntity entity) {
        if (entity == null) {
            return List.of();
        }
        return operationJpaRepository
                .findByAffectedEntityTypeAndAffectedEntityId(entity.auditableType(), entity.auditableId())
                .stream()
                .map(operationMapper::toDomain)
                .toList();
    }
}
