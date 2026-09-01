package application.domain.ports.out;

import application.domain.models.AuditableEntity;
import application.domain.models.Operation;
import application.domain.models.User;
import java.util.List;

/** Persistence contract for business {@link Operation} records. */
public interface OperationRepositoryPort {

    Operation save(Operation operation);

    List<Operation> findByUser(User user);

    List<Operation> findByEntity(AuditableEntity entity);
}
