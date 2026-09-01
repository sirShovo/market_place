package application.domain.services.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.exceptions.DuplicateUserException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.support.Fakes;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.UserStatus;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegisterUserServiceTest {

    private FakeUserRepo repo;
    private RegisterUserService service;

    @BeforeEach
    void setUp() {
        repo = new FakeUserRepo();
        Fakes.OperationStore ops = new Fakes.OperationStore();
        Fakes.AuditStore audits = new Fakes.AuditStore();
        service = new RegisterUserService(
                repo,
                new Fakes.PlainPasswords(),
                new ValidateUserStatusService(),
                new ValidateRoleAuthorizationService(),
                Fakes.auditService(ops, audits));
    }

    private User admin() {
        User u = new User();
        u.setUsername("admin");
        u.setRole(UserRole.ADMIN);
        u.setStatus(UserStatus.ACTIVE);
        return u;
    }

    private User newSeller() {
        User u = new User();
        u.setUsername("seller1");
        u.setPassword("secret");
        u.setRole(UserRole.SELLER);
        u.setIdentification(new DocumentId("900123"));
        u.setEmail(new Email("seller1@shop.com"));
        return u;
    }

    @Test
    void adminRegistersSellerAndPasswordIsEncrypted() {
        User saved = service.register(admin(), newSeller());
        assertEquals("enc:secret", saved.getPassword());
        assertEquals(UserStatus.ACTIVE, saved.getStatus());
    }

    @Test
    void nonAdminCannotRegisterSeller() {
        User requester = admin();
        requester.setRole(UserRole.SUPERVISOR);
        assertThrows(UnauthorizedOperationException.class,
                () -> service.register(requester, newSeller()));
    }

    @Test
    void duplicateDocumentIsRejected() {
        repo.existsByIdentification = true;
        assertThrows(DuplicateUserException.class,
                () -> service.register(admin(), newSeller()));
    }

    private static final class FakeUserRepo implements UserRepositoryPort {
        boolean existsByIdentification;
        boolean existsByEmail;

        @Override
        public User save(User user) {
            return user;
        }

        @Override
        public Optional<User> findByUsername(User user) {
            return Optional.empty();
        }

        @Override
        public Optional<User> findByIdentification(User user) {
            return Optional.empty();
        }

        @Override
        public boolean existsByIdentification(User user) {
            return existsByIdentification;
        }

        @Override
        public boolean existsByEmail(User user) {
            return existsByEmail;
        }

        @Override
        public void update(User user) {
        }
    }
}
