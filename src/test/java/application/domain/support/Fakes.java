package application.domain.support;

import application.domain.models.AuditLog;
import application.domain.models.AuditableEntity;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.out.AuditLogRepositoryPort;
import application.domain.ports.out.OperationRepositoryPort;
import application.domain.ports.out.PasswordServicePort;
import application.domain.services.operation.RegisterAuditLogService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.services.operation.RegisterOperationService;
import java.util.ArrayList;
import java.util.List;

/** Lightweight hand-written test doubles shared by the domain service tests. */
public final class Fakes {

    private Fakes() {
    }

    /** No-op operation store. */
    public static final class OperationStore implements OperationRepositoryPort {
        public final List<Operation> saved = new ArrayList<>();

        @Override
        public Operation save(Operation operation) {
            saved.add(operation);
            return operation;
        }

        @Override
        public List<Operation> findByUser(User user) {
            return saved;
        }

        @Override
        public List<Operation> findByEntity(AuditableEntity entity) {
            return saved;
        }
    }

    /** No-op audit store. */
    public static final class AuditStore implements AuditLogRepositoryPort {
        public final List<AuditLog> saved = new ArrayList<>();

        @Override
        public AuditLog save(AuditLog auditLog) {
            saved.add(auditLog);
            return auditLog;
        }

        @Override
        public List<AuditLog> findByUser(User user) {
            return saved;
        }

        @Override
        public List<AuditLog> findByEntity(AuditableEntity entity) {
            return saved;
        }
    }

    /** Trivial password service. */
    public static final class PlainPasswords implements PasswordServicePort {
        @Override
        public String encrypt(String rawPassword) {
            return "enc:" + rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String encodedPassword) {
            return encodedPassword.equals("enc:" + rawPassword);
        }
    }

    /** Builds a real audit collaborator backed by the given stores. */
    public static RegisterOperationAndAuditService auditService(OperationStore ops, AuditStore audits) {
        return new RegisterOperationAndAuditService(
                new RegisterOperationService(ops),
                new RegisterAuditLogService(audits));
    }
}
