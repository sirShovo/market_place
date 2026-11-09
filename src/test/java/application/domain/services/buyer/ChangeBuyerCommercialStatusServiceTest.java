package application.domain.services.buyer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.support.Fakes;
import application.domain.valueobjects.BuyerCommercialStatus;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.UserStatus;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChangeBuyerCommercialStatusServiceTest {

    private FakeBuyerRepo buyerRepo;
    private Fakes.OperationStore ops;
    private ChangeBuyerCommercialStatusService service;

    @BeforeEach
    void setUp() {
        buyerRepo = new FakeBuyerRepo();
        ops = new Fakes.OperationStore();
        service = new ChangeBuyerCommercialStatusService(
                buyerRepo,
                new ValidateUserStatusService(),
                new ValidateRoleAuthorizationService(),
                Fakes.auditService(ops, new Fakes.AuditStore()));
    }

    private User admin() {
        User u = new User();
        u.setRole(UserRole.ADMIN);
        u.setStatus(UserStatus.ACTIVE);
        return u;
    }

    private Buyer activeBuyer() {
        Buyer b = new Buyer();
        b.setIdentification(new DocumentId("100200"));
        b.setEmail(new Email("buyer@shop.com"));
        b.setCommercialStatus(BuyerCommercialStatus.ACTIVE);
        return b;
    }

    @Test
    void adminSuspendsBuyerAndLogsOperation() {
        buyerRepo.stored = activeBuyer();
        Buyer probe = new Buyer();
        probe.setIdentification(new DocumentId("100200"));
        probe.setCommercialStatus(BuyerCommercialStatus.SUSPENDED);

        Buyer updated = service.changeCommercialStatus(admin(), probe);
        assertEquals(BuyerCommercialStatus.SUSPENDED, updated.getCommercialStatus());
        assertEquals(1, ops.saved.size());
        assertEquals(OperationType.USER_STATUS_CHANGE, ops.saved.get(0).getOperationType());
    }

    @Test
    void nonAdminCannotChangeBuyerCommercialStatus() {
        buyerRepo.stored = activeBuyer();
        User supervisor = admin();
        supervisor.setRole(UserRole.SUPERVISOR);
        Buyer probe = new Buyer();
        probe.setIdentification(new DocumentId("100200"));
        probe.setCommercialStatus(BuyerCommercialStatus.SUSPENDED);

        assertThrows(UnauthorizedOperationException.class,
                () -> service.changeCommercialStatus(supervisor, probe));
    }

    @Test
    void missingBuyerRaisesNotFound() {
        buyerRepo.stored = null;
        Buyer probe = new Buyer();
        probe.setIdentification(new DocumentId("999"));
        probe.setCommercialStatus(BuyerCommercialStatus.SUSPENDED);

        assertThrows(EntityNotFoundException.class,
                () -> service.changeCommercialStatus(admin(), probe));
    }

    private static final class FakeBuyerRepo implements BuyerRepositoryPort {
        Buyer stored;

        @Override
        public Buyer save(Buyer buyer) {
            stored = buyer;
            return buyer;
        }

        @Override
        public Optional<Buyer> findByIdentification(Buyer buyer) {
            return Optional.ofNullable(stored);
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
        public void update(Buyer buyer) {
            stored = buyer;
        }
    }
}
