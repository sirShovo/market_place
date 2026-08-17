package com.marketplace.domain.model.entity;

import java.time.LocalDateTime;

/**
 * Domain entity representing an Audit Log entry.
 * 
 * Used to trace and persist critical business operations, state changes, 
 * and user actions for security and debugging purposes.
 */
public class AuditLog {
    private Long id;
    private String entityName;
    private Long entityId;
    private String action;
    private String detailData;
    private Long userId;
    private LocalDateTime timestamp;

    /**
     * Default constructor for JPA and Mappers. Should not be used directly for domain logic.
     */
    protected AuditLog() {}

    private AuditLog(String entityName, Long entityId, String action, String detailData, Long userId) {
        this.entityName = entityName;
        this.entityId = entityId;
        this.action = action;
        this.detailData = detailData;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Factory method to create a new AuditLog entry.
     *
     * @param entityName The name of the entity affected (e.g., "Order", "Product").
     * @param entityId   The unique identifier of the affected entity.
     * @param action     The action performed (e.g., "CREATE", "CANCEL", "PRICE_UPDATE").
     * @param detailData JSON string or detailed description of the changes.
     * @param userId     The ID of the user who performed the action.
     * @return A new AuditLog entity instance.
     */
    public static AuditLog create(String entityName, Long entityId, String action, String detailData, Long userId) {
        return new AuditLog(entityName, entityId, action, detailData, userId);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEntityName() { return entityName; }
    public Long getEntityId() { return entityId; }
    public String getAction() { return action; }
    public String getDetailData() { return detailData; }
    public Long getUserId() { return userId; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
