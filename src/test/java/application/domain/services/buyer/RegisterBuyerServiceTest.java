package application.domain.services.buyer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.DuplicateUserException;
import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.support.Fakes;
import application.domain.valueobjects.BuyerCommercialStatus;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.UserRole;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegisterBuyerServiceTest {

    private FakeBuyerRepo buyerRepo;
    private FakeUserRepo userRepo;
    private Fakes.NoopSellerRepo sellerRepo;
    private RegisterBuyerService service;

    @BeforeEach
    void setUp() {
        buyerRepo = new FakeBuyerRepo();
        userRepo = new FakeUserRepo();
        sellerRepo = new Fakes.NoopSellerRepo();
        Fakes.OperationStore ops = new Fakes.OperationStore();
        Fakes.AuditStore audits = new Fakes.AuditStore();
        service = new RegisterBuyerService(
                buyerRepo,
                userRepo,
                new Fakes.PlainPasswords(),
                Fakes.uniquenessService(userRepo, buyerRepo, sellerRepo),
                Fakes.auditService(ops, audits));
    }

    private Buyer newBuyer() {
        Buyer buyer = new Buyer();
        buyer.setFullName("Jane Doe");
        buyer.setIdentification(new DocumentId("123456"));
        buyer.setEmail(new Email("jane@doe.com"));
        buyer.setMainAddress("123 Main St");
        return buyer;
    }

    private User account() {
        User account = new User();
        account.setUsername("janedoe");
        account.setPassword("secret");
        return account;
    }

    @Test
    void registersBuyerAndCreatesALoginAccountWithSharedIdentity() {
        Buyer savedBuyer = service.register(newBuyer(), account());

        assertEquals(UserRole.BUYER, savedBuyer.getRole());
        assertEquals(BuyerCommercialStatus.ACTIVE, savedBuyer.getCommercialStatus());
        assertEquals(1, userRepo.saved.size());
        User savedAccount = userRepo.saved.get(0);
        assertEquals("enc:secret", savedAccount.getPassword());
        assertEquals(UserRole.BUYER, savedAccount.getRole());
        assertEquals(savedBuyer.getIdentification(), savedAccount.getIdentification());
    }

    @Test
    void missingMainAddressIsRejected() {
        Buyer buyer = newBuyer();
        buyer.setMainAddress(null);
        assertThrows(DomainException.class, () -> service.register(buyer, account()));
    }

    @Test
    void missingCredentialsAreRejected() {
        assertThrows(DomainException.class, () -> service.register(newBuyer(), new User()));
    }

    @Test
    void documentAlreadyUsedBySellerIsRejectedPlatformWide() {
        sellerRepo.identificationTaken = true;
        assertThrows(DuplicateUserException.class, () -> service.register(newBuyer(), account()));
    }

    @Test
    void duplicateUsernameIsRejected() {
        userRepo.usernameTaken = true;
        assertThrows(DuplicateUserException.class, () -> service.register(newBuyer(), account()));
    }

    private static final class FakeBuyerRepo implements BuyerRepositoryPort {
        boolean identificationTaken;
        boolean emailTaken;

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

    private static final class FakeUserRepo implements UserRepositoryPort {
        boolean usernameTaken;
        final java.util.List<User> saved = new java.util.ArrayList<>();

        @Override
        public User save(User user) {
            saved.add(user);
            return user;
        }

        @Override
        public Optional<User> findByUsername(User user) {
            return usernameTaken ? Optional.of(user) : Optional.empty();
        }

        @Override
        public Optional<User> findByIdentification(User user) {
            return Optional.empty();
        }

        @Override
        public boolean existsByIdentification(DocumentId identification) {
            return false;
        }

        @Override
        public boolean existsByEmail(Email email) {
            return false;
        }

        @Override
        public void update(User user) {
        }
    }
}
