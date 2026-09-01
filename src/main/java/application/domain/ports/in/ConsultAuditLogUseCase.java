package application.domain.ports.in;

import application.domain.models.AuditLog;
import application.domain.models.AuditableEntity;
import application.domain.models.User;
import java.util.List;

/** Reads audit history for an entity ({@code ADMIN} / {@code SUPERVISOR}). */
public interface ConsultAuditLogUseCase {

    List<AuditLog> consult(User requester, AuditableEntity entity);
}
