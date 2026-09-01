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
}
