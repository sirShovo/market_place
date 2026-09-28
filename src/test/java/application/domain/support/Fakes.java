package application.domain.support;

import application.domain.models.AuditLog;
import application.domain.models.AuditableEntity;
import application.domain.models.Buyer;
import application.domain.models.Operation;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.out.AuditLogRepositoryPort;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.OperationRepositoryPort;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.identity.ValidatePlatformUniquenessService;
import application.domain.services.operation.RegisterAuditLogService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.services.operation.RegisterOperationService;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    /**
     * A {@link BuyerRepositoryPort} stub whose uniqueness answers are configurable and
     * whose mutating methods are no-ops. Used to compose {@link ValidatePlatformUniquenessService}
     * in tests that are not themselves about the buyer subdomain.
     */
    public static final class NoopBuyerRepo implements BuyerRepositoryPort {
        public boolean identificationTaken;
        public boolean emailTaken;

        @Override
        public Buyer save(Buyer buyer) {
            return buyer;
        }

        @Override
        public Optional<Buyer> findByIdentification(Buyer buyer) {
            return Optional.empty();
        }

        @Override
        public boolean existsByIdentification(DocumentId identification) {
            return identificationTaken;
        }

        @Override
        public boolean existsByEmail(Email email) {
            return emailTaken;
        }

        @Override
        public void update(Buyer buyer) {
        }
    }

    /**
     * A {@link SellerRepositoryPort} stub whose uniqueness answers are configurable and
     * whose mutating methods are no-ops. Used to compose {@link ValidatePlatformUniquenessService}
     * in tests that are not themselves about the seller subdomain.
     */
    public static final class NoopSellerRepo implements SellerRepositoryPort {
        public boolean identificationTaken;
        public boolean emailTaken;

        @Override
        public Seller save(Seller seller) {
            return seller;
        }

        @Override
        public Optional<Seller> findByIdentification(Seller seller) {
            return Optional.empty();
        }

        @Override
        public boolean existsByIdentification(DocumentId identification) {
            return identificationTaken;
        }

        @Override
        public boolean existsByEmail(Email email) {
            return emailTaken;
        }

        @Override
        public List<Seller> findAll() {
            return List.of();
        }

        @Override
        public void update(Seller seller) {
        }
    }

    /** Builds a real audit collaborator backed by the given stores. */
    public static RegisterOperationAndAuditService auditService(OperationStore ops, AuditStore audits) {
        return new RegisterOperationAndAuditService(
                new RegisterOperationService(ops),
                new RegisterAuditLogService(audits));
    }

    /**
     * Builds a real {@link ValidatePlatformUniquenessService}. Pass {@link NoopBuyerRepo} /
     * {@link NoopSellerRepo} (both defaulting to "nothing taken") when the test under
     * exercise is not itself about buyer/seller uniqueness.
     */
    public static ValidatePlatformUniquenessService uniquenessService(
            UserRepositoryPort users, BuyerRepositoryPort buyers, SellerRepositoryPort sellers) {
        return new ValidatePlatformUniquenessService(users, buyers, sellers);
    }
}
