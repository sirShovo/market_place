package application.domain.ports.out;

import application.domain.models.AuditLog;
import application.domain.models.AuditableEntity;
import application.domain.models.User;
import java.util.List;

/**
 * Persistence contract for immutable {@link AuditLog} records. Expected MongoDB
 * implementation; the domain stays unaware of that.
 */
public interface AuditLogRepositoryPort {

    AuditLog save(AuditLog auditLog);

    List<AuditLog> findByUser(User user);

    List<AuditLog> findByEntity(AuditableEntity entity);
}
