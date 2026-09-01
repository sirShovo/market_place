package application.domain.model.entity;
import java.time.LocalDateTime;

/**
 * Domain entity representing a system audit log.
 */
public class AuditLog {
    private Long id;
    private String action;
    private LocalDateTime timestamp;
    
    /**
     * Constructor for AuditLog.
     * @param action The action description.
     */
    public AuditLog(String action) { this.action = action; this.timestamp = LocalDateTime.now(); }
    
    /** @return The AuditLog ID */
    public Long getId() { return id; }
    /** @param id The AuditLog ID to set */
    public void setId(Long id) { this.id = id; }
}
