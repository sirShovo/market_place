package application.domain.services.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.User;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.authorization.ValidateBuyerOwnershipService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.UserStatus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsultBuyerOrdersServiceTest {

    private ConsultBuyerOrdersService service;
    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = new Order();
        sampleOrder.setIdentifier("ORD-77");

        OrderRepositoryPort repo = new OrderRepositoryPort() {
            @Override
            public Order save(Order order) {
                return order;
            }

            @Override
            public Optional<Order> findByIdentifier(Order order) {
                return Optional.of(sampleOrder);
            }

            @Override
            public List<Order> findByBuyer(Buyer buyer) {
                return List.of(sampleOrder);
            }

            @Override
            public void update(Order order) {
            }
        };

        service = new ConsultBuyerOrdersService(
                repo,
                new ValidateUserStatusService(),
                new ValidateBuyerOwnershipService());
    }

    private User user(UserRole role, String doc) {
        User u = new User();
        u.setRole(role);
        u.setStatus(UserStatus.ACTIVE);
        u.setIdentification(new DocumentId(doc));
        return u;
    }

    private Buyer buyer(String doc) {
        Buyer b = new Buyer();
        b.setIdentification(new DocumentId(doc));
        return b;
    }

    @Test
    void buyerListsItsOwnOrders() {
        List<Order> result = service.consultByBuyer(user(UserRole.BUYER, "1001"), buyer("1001"));
        assertEquals(1, result.size());
        assertEquals("ORD-77", result.get(0).getIdentifier());
    }

    @Test
    void buyerCannotListAnotherBuyersOrders() {
        assertThrows(UnauthorizedOperationException.class,
                () -> service.consultByBuyer(user(UserRole.BUYER, "2002"), buyer("1001")));
    }

    @Test
    void adminAndSupervisorCanListAnyBuyersOrders() {
        assertEquals(1, service.consultByBuyer(user(UserRole.ADMIN, "1"), buyer("1001")).size());
        assertEquals(1, service.consultByBuyer(user(UserRole.SUPERVISOR, "2"), buyer("1001")).size());
    }
}
