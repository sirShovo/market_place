package application.domain.models;

import application.domain.valueobjects.OperationType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A significant business action executed over an {@link AuditableEntity}. Represents
 * the action that occurred, distinct from the current status of the affected entity.
 */
@Getter
@Setter
@NoArgsConstructor
public class Operation {

    private Integer operationId;
    private OperationType operationType;
    private LocalDateTime executionDate;
    private User performedBy;
    private AuditableEntity affectedEntity;

    /** Convenience factory for the common case. */
    public static Operation of(OperationType type, User performedBy, AuditableEntity affectedEntity) {
        Operation operation = new Operation();
        operation.setOperationType(type);
        operation.setPerformedBy(performedBy);
        operation.setAffectedEntity(affectedEntity);
        operation.setExecutionDate(LocalDateTime.now());
        return operation;
    }
}
