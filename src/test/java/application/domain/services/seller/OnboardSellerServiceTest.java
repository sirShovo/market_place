package application.domain.services.seller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.DuplicateUserException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.support.Fakes;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.SellerStatus;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.UserStatus;
import application.domain.valueobjects.WarehouseType;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OnboardSellerServiceTest {

    private FakeSellerRepo sellerRepo;
    private FakeWarehouseRepo warehouseRepo;
    private FakeUserRepo userRepo;
    private Fakes.NoopBuyerRepo buyerRepo;
    private OnboardSellerService service;

    @BeforeEach
    void setUp() {
        sellerRepo = new FakeSellerRepo();
        warehouseRepo = new FakeWarehouseRepo();
        userRepo = new FakeUserRepo();
        buyerRepo = new Fakes.NoopBuyerRepo();
        service = new OnboardSellerService(
                sellerRepo,
                warehouseRepo,
                userRepo,
                new Fakes.PlainPasswords(),
                new ValidateUserStatusService(),
                new ValidateRoleAuthorizationService(),
                Fakes.uniquenessService(userRepo, buyerRepo, sellerRepo),
                Fakes.auditService(new Fakes.OperationStore(), new Fakes.AuditStore()));
    }

    private User admin() {
        User admin = new User();
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        return admin;
    }

    private Seller newSeller() {
        Seller seller = new Seller();
        seller.setIdentification(new DocumentId("900555"));
        seller.setEmail(new Email("shop@seller.com"));
        return seller;
    }

    private User account() {
        User account = new User();
        account.setUsername("shopowner");
        account.setPassword("secret");
        return account;
    }

    private Warehouse firstWarehouse() {
        Warehouse warehouse = new Warehouse();
        warehouse.setIdentifier("W-1");
        return warehouse;
    }

    @Test
    void onboardsSellerWithFirstWarehouseAndLoginAccount() {
        Seller saved = service.onboard(admin(), newSeller(), firstWarehouse(), account());

        assertEquals(UserRole.SELLER, saved.getRole());
        assertEquals(SellerStatus.ACTIVE, saved.getStatus());
        assertEquals(1, saved.getWarehouses().size());
        assertEquals(WarehouseType.SELLER, saved.getWarehouses().get(0).getType());
        assertEquals(1, userRepo.saved.size());
        assertEquals("enc:secret", userRepo.saved.get(0).getPassword());
        assertEquals(saved.getIdentification(), userRepo.saved.get(0).getIdentification());
    }

    @Test
    void nonAdminCannotOnboardASeller() {
        User requester = admin();
        requester.setRole(UserRole.SUPERVISOR);
        assertThrows(UnauthorizedOperationException.class,
                () -> service.onboard(requester, newSeller(), new Warehouse(), account()));
    }

    @Test
    void missingFirstWarehouseIsRejected() {
        assertThrows(DomainException.class,
                () -> service.onboard(admin(), newSeller(), null, account()));
    }

    @Test
    void missingCredentialsAreRejected() {
        assertThrows(DomainException.class,
                () -> service.onboard(admin(), newSeller(), new Warehouse(), new User()));
    }

    @Test
    void documentAlreadyUsedByABuyerIsRejectedPlatformWide() {
        buyerRepo.identificationTaken = true;
        assertThrows(DuplicateUserException.class,
                () -> service.onboard(admin(), newSeller(), new Warehouse(), account()));
    }

    private static final class FakeSellerRepo implements SellerRepositoryPort {
        boolean identificationTaken;
        boolean emailTaken;

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
        public java.util.List<Seller> findAll() {
            return java.util.List.of();
        }

        @Override
        public void update(Seller seller) {
        }
    }

    private static final class FakeWarehouseRepo implements WarehouseRepositoryPort {
        @Override
        public Warehouse save(Warehouse warehouse) {
            return warehouse;
        }

        @Override
        public Optional<Warehouse> findByIdentifier(Warehouse warehouse) {
            return Optional.empty();
        }

        @Override
        public java.util.List<Warehouse> findByOwner(Seller owner) {
            return java.util.List.of();
        }

        @Override
        public void update(Warehouse warehouse) {
        }
    }

    private static final class FakeUserRepo implements UserRepositoryPort {
        final java.util.List<User> saved = new java.util.ArrayList<>();

        @Override
        public User save(User user) {
            saved.add(user);
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
