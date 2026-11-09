package application.domain.models;

/**
 * Marker implemented by entities that can be the subject of an {@link Operation} and
 * an {@link AuditLog} record.
 */
public interface AuditableEntity {

    /** Business type name of the entity (e.g. {@code "Order"}). */
    String auditableType();

    /** Business identifier of the entity, as text. */
    String auditableId();

    /**
     * Reconstructs a lightweight {@link AuditableEntity} reference from stored type and id.
     */
    static AuditableEntity of(String type, String id) {
        if (type == null && id == null) {
            return null;
        }
        return new AuditableEntity() {
            @Override
            public String auditableType() {
                return type;
            }

            @Override
            public String auditableId() {
                return id;
            }
        };
    }
}
