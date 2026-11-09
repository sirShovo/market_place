package application.adapters.in.rest.responses;

import java.time.LocalDateTime;
import java.util.Map;

public record AuditLogResponse(
        String auditId,
        String operationType,
        LocalDateTime operationDate,
        String performedByUsername,
        String userRole,
        String affectedEntityType,
        String affectedEntityId,
        String severity,
        Map<String, Object> details
) {
}
