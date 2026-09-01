package application.domain.services.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.exceptions.InvalidReservationException;
import application.domain.exceptions.NegativeStockException;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.PhysicalProduct;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.support.Fakes;
import application.domain.valueobjects.InventoryItemCondition;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.StockQuantity;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.UserStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReserveInventoryServiceTest {

    private FakeInventoryRepo repo;
    private ReserveInventoryService service;

    @BeforeEach
    void setUp() {
        repo = new FakeInventoryRepo();
        service = new ReserveInventoryService(
                repo,
                new ValidateUserStatusService(),
                new ValidateRoleAuthorizationService(),
                Fakes.auditService(new Fakes.OperationStore(), new Fakes.AuditStore()));
    }

    private User operator() {
        User u = new User();
        u.setRole(UserRole.LOGISTICS_OPERATOR);
        u.setStatus(UserStatus.ACTIVE);
        return u;
    }

    private InventoryItem item(int stock, InventoryItemCondition condition) {
        InventoryItem i = new InventoryItem();
        i.setProduct(new PhysicalProduct());
        i.setWarehouse(new Warehouse());
        i.setStock(StockQuantity.of(stock));
        i.setCondition(condition);
        return i;
    }

    @Test
    void reservesAndRecordsAMovement() {
        repo.stored = item(10, InventoryItemCondition.AVAILABLE);
        InventoryMovement movement = service.reserve(operator(), new InventoryItem(), 3);
        assertEquals(7, repo.stored.getStock().value());
        assertEquals(InventoryMovementType.RESERVATION, movement.getType());
    }

    @Test
    void rejectsReservationOnMissingInventory() {
        repo.stored = null;
        assertThrows(InvalidReservationException.class,
                () -> service.reserve(operator(), new InventoryItem(), 1));
    }

    @Test
    void rejectsReservationOnDamagedInventory() {
        repo.stored = item(10, InventoryItemCondition.DAMAGED);
        assertThrows(InvalidReservationException.class,
                () -> service.reserve(operator(), new InventoryItem(), 1));
    }

    @Test
    void rejectsReservationBeyondAvailableStock() {
        repo.stored = item(2, InventoryItemCondition.AVAILABLE);
        assertThrows(NegativeStockException.class,
                () -> service.reserve(operator(), new InventoryItem(), 5));
    }

    private static final class FakeInventoryRepo implements InventoryRepositoryPort {
        InventoryItem stored;
        final List<InventoryMovement> movements = new ArrayList<>();

        @Override
        public InventoryItem save(InventoryItem item) {
            stored = item;
            return item;
        }

        @Override
        public Optional<InventoryItem> findByProductAndWarehouse(InventoryItem probe) {
            return Optional.ofNullable(stored);
        }

        @Override
        public List<InventoryItem> findByProduct(application.domain.models.Product product) {
            return stored == null ? List.of() : List.of(stored);
        }

        @Override
        public void update(InventoryItem item) {
            stored = item;
        }

        @Override
        public InventoryMovement saveMovement(InventoryMovement movement) {
            movements.add(movement);
            return movement;
        }

        @Override
        public List<InventoryMovement> findMovements(InventoryItem item) {
            return movements;
        }
    }
}
