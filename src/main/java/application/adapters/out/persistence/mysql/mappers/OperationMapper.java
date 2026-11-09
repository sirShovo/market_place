package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.OperationJpaEntity;
import application.domain.models.AuditableEntity;
import application.domain.models.Operation;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OperationMapper {

    private final UserMapper userMapper;

    public OperationJpaEntity toEntity(Operation domain) {
        if (domain == null) {
            return null;
        }
        OperationJpaEntity entity = new OperationJpaEntity();
        entity.setOperationId(domain.getOperationId());
        entity.setOperationType(domain.getOperationType() != null ? domain.getOperationType().getCode() : null);
        entity.setExecutionDate(domain.getExecutionDate());
        entity.setPerformedBy(userMapper.toEntity(domain.getPerformedBy()));
        if (domain.getAffectedEntity() != null) {
            entity.setAffectedEntityType(domain.getAffectedEntity().auditableType());
            entity.setAffectedEntityId(domain.getAffectedEntity().auditableId());
        }
        return entity;
    }

    public Operation toDomain(OperationJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Operation domain = new Operation();
        domain.setOperationId(entity.getOperationId());
        domain.setOperationType(OperationType.fromCode(entity.getOperationType()));
        domain.setExecutionDate(entity.getExecutionDate());
        domain.setPerformedBy(userMapper.toDomain(entity.getPerformedBy()));
        domain.setAffectedEntity(
                AuditableEntity.of(entity.getAffectedEntityType(), entity.getAffectedEntityId()));
        return domain;
    }
}
