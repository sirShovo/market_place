package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.OperationJpaEntity;
import application.adapters.out.persistence.mysql.mappers.OperationMapper;
import application.adapters.out.persistence.mysql.repositories.OperationJpaRepository;
import application.domain.models.AuditableEntity;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.out.OperationRepositoryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OperationMySqlAdapter implements OperationRepositoryPort {

    private final OperationJpaRepository operationJpaRepository;
    private final OperationMapper operationMapper;

    @Override
    public Operation save(Operation operation) {
        OperationJpaEntity saved = operationJpaRepository.save(operationMapper.toEntity(operation));
        return operationMapper.toDomain(saved);
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
