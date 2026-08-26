package domain.model.entity;
import java.time.LocalDateTime;
public class AuditLog {
    private Long id;
    private String action;
    private LocalDateTime timestamp;
    public AuditLog(String action) { this.action = action; this.timestamp = LocalDateTime.now(); }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}
