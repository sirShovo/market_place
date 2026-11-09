package application.adapters.out.persistence.mongodb.documents;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLogDocument {

    @Id
    private String auditId;

    private String operationType;
    private LocalDateTime operationDate;
    private Integer performedByUserId;
    private String performedByUsername;
    private String performedByIdentification;
    private String userRole;
    private String affectedEntityType;
    private String affectedEntityId;
    private String severity;
    private Map<String, Object> details;
}
