package application.adapters.out.persistence.mongodb.adapters;

import application.adapters.out.persistence.mongodb.documents.AuditLogDocument;
import application.adapters.out.persistence.mongodb.mappers.AuditLogDocumentMapper;
import application.adapters.out.persistence.mongodb.repositories.AuditLogMongoRepository;
import application.domain.models.AuditLog;
import application.domain.models.AuditableEntity;
import application.domain.models.User;
import application.domain.ports.out.AuditLogRepositoryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditLogMongoAdapter implements AuditLogRepositoryPort {

    private final AuditLogMongoRepository auditLogMongoRepository;
    private final AuditLogDocumentMapper auditLogDocumentMapper;

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogDocument saved = auditLogMongoRepository.save(auditLogDocumentMapper.toDocument(auditLog));
        return auditLogDocumentMapper.toDomain(saved);
    }

    @Override
    public List<AuditLog> findByUser(User user) {
        if (user == null || user.getUsername() == null) {
            return List.of();
        }
        return auditLogMongoRepository.findByPerformedByUsername(user.getUsername())
                .stream()
                .map(auditLogDocumentMapper::toDomain)
                .toList();
    }

    @Override
    public List<AuditLog> findByEntity(AuditableEntity entity) {
        if (entity == null) {
            return List.of();
        }
        return auditLogMongoRepository
                .findByAffectedEntityTypeAndAffectedEntityId(entity.auditableType(), entity.auditableId())
                .stream()
                .map(auditLogDocumentMapper::toDomain)
                .toList();
    }
}
