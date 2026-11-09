package application.adapters.out.persistence.mongodb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import application.adapters.out.persistence.mongodb.adapters.AuditLogMongoAdapter;
import application.adapters.out.persistence.mongodb.documents.AuditLogDocument;
import application.adapters.out.persistence.mongodb.mappers.AuditLogDocumentMapper;
import application.adapters.out.persistence.mongodb.repositories.AuditLogMongoRepository;
import application.domain.enums.AuditSeverity;
import application.domain.models.AuditLog;
import application.domain.models.Order;
import application.domain.models.User;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.UserRole;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AuditLogMongoAdapterTest {

    @Test
    void auditLogMongoAdapterSavesAndQueriesAndMapsCorrectly() {
        AuditLogMongoRepository repo = mock(AuditLogMongoRepository.class);
        AuditLogDocumentMapper mapper = new AuditLogDocumentMapper();
        AuditLogMongoAdapter adapter = new AuditLogMongoAdapter(repo, mapper);

        User actor = new User();
        actor.setUserId(10);
        actor.setUsername("admin");
        actor.setIdentification(new DocumentId("DOC-ADMIN"));
        actor.setRole(UserRole.ADMIN);

        Order order = new Order();
        order.setIdentifier("ORD-99");

        AuditLog log = new AuditLog();
        log.setAuditId("AUD-1");
        log.setOperationType(OperationType.ORDER_PAYMENT);
        log.setOperationDate(LocalDateTime.now());
        log.setPerformedBy(actor);
        log.setUserRole(UserRole.ADMIN);
        log.setAffectedEntity(order);
        log.setSeverity(AuditSeverity.INFO);
        log.setDetails(Map.of("amount", 100));

        when(repo.save(any(AuditLogDocument.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repo.findByAffectedEntityTypeAndAffectedEntityId("Order", "ORD-99"))
                .thenReturn(List.of(mapper.toDocument(log)));

        AuditLog saved = adapter.save(log);
        assertEquals("AUD-1", saved.getAuditId());
        assertEquals(OperationType.ORDER_PAYMENT, saved.getOperationType());
        assertEquals("admin", saved.getPerformedBy().getUsername());
        assertEquals("Order", saved.getAffectedEntity().auditableType());
        assertEquals("ORD-99", saved.getAffectedEntity().auditableId());

        List<AuditLog> found = adapter.findByEntity(order);
        assertEquals(1, found.size());
        assertEquals(100, found.get(0).getDetails().get("amount"));
    }
}
