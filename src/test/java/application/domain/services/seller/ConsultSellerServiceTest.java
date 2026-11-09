package application.domain.services.seller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.services.authorization.ValidateSellerOwnershipService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.UserStatus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsultSellerServiceTest {

    private ConsultSellerService service;
    private Seller storedSeller;

    @BeforeEach
    void setUp() {
        storedSeller = new Seller();
        storedSeller.setIdentification(new DocumentId("900555"));
        storedSeller.setFullName("Acme Supplies");

        SellerRepositoryPort repo = new SellerRepositoryPort() {
            @Override
            public Seller save(Seller seller) {
                return seller;
            }

            @Override
            public Optional<Seller> findByIdentification(Seller seller) {
                return Optional.of(storedSeller);
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
            public List<Seller> findAll() {
                return List.of(storedSeller);
            }

            @Override
            public void update(Seller seller) {
            }
        };

        service = new ConsultSellerService(
                repo,
                new ValidateUserStatusService(),
                new ValidateSellerOwnershipService());
    }

    private User user(UserRole role, String doc) {
        User u = new User();
        u.setRole(role);
        u.setStatus(UserStatus.ACTIVE);
        u.setIdentification(new DocumentId(doc));
        return u;
    }

    @Test
    void sellerCanConsultItsOwnProfile() {
        Seller probe = new Seller();
        probe.setIdentification(new DocumentId("900555"));

        Seller result = service.consult(user(UserRole.SELLER, "900555"), probe);
        assertEquals("Acme Supplies", result.getFullName());
    }

    @Test
    void sellerCannotConsultAnotherSellersProfile() {
        Seller probe = new Seller();
        probe.setIdentification(new DocumentId("900555"));

        assertThrows(UnauthorizedOperationException.class,
                () -> service.consult(user(UserRole.SELLER, "900999"), probe));
    }

    @Test
    void adminAndSupervisorCanConsultAnySeller() {
        Seller probe = new Seller();
        probe.setIdentification(new DocumentId("900555"));

        assertEquals(storedSeller, service.consult(user(UserRole.ADMIN, "1"), probe));
        assertEquals(storedSeller, service.consult(user(UserRole.SUPERVISOR, "2"), probe));
    }
}
