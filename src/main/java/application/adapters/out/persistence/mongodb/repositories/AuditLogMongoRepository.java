package application.adapters.out.persistence.mongodb.repositories;

import application.adapters.out.persistence.mongodb.documents.AuditLogDocument;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditLogMongoRepository extends MongoRepository<AuditLogDocument, String> {

    List<AuditLogDocument> findByPerformedByUsername(String username);

    List<AuditLogDocument> findByAffectedEntityTypeAndAffectedEntityId(
            String affectedEntityType, String affectedEntityId);
}
