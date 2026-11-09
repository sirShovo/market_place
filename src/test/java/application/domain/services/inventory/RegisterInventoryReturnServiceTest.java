package application.domain.services.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.PhysicalProduct;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.support.Fakes;
import application.domain.valueobjects.InventoryItemCondition;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.StockQuantity;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.UserStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegisterInventoryReturnServiceTest {

    private FakeInventoryRepo repo;
    private Fakes.OperationStore ops;
    private RegisterInventoryReturnService service;

    @BeforeEach
    void setUp() {
        repo = new FakeInventoryRepo();
        ops = new Fakes.OperationStore();
        service = new RegisterInventoryReturnService(
                repo,
                new ValidateUserStatusService(),
                new ValidateRoleAuthorizationService(),
                Fakes.auditService(ops, new Fakes.AuditStore()));
    }

    private User operator() {
        User u = new User();
        u.setRole(UserRole.LOGISTICS_OPERATOR);
        u.setStatus(UserStatus.ACTIVE);
        return u;
    }

    private InventoryItem item(int stock) {
        InventoryItem i = new InventoryItem();
        i.setId(1L);
        i.setProduct(new PhysicalProduct());
        i.setWarehouse(new Warehouse());
        i.setStock(StockQuantity.of(stock));
        i.setCondition(InventoryItemCondition.AVAILABLE);
        return i;
    }

    @Test
    void incrementsStockAndRecordsReturnMovement() {
        repo.stored = item(5);
        InventoryMovement movement = service.registerReturn(operator(), new InventoryItem(), 3);
        assertEquals(8, repo.stored.getStock().value());
        assertEquals(InventoryMovementType.RETURN, movement.getType());
        assertEquals(1, ops.saved.size());
        assertEquals(OperationType.INVENTORY_RETURN, ops.saved.get(0).getOperationType());
    }

    @Test
    void rejectsNonPositiveReturnQuantity() {
        repo.stored = item(5);
        assertThrows(DomainException.class,
                () -> service.registerReturn(operator(), new InventoryItem(), 0));
    }

    @Test
    void rejectsReturnWhenInventoryItemDoesNotExist() {
        repo.stored = null;
        assertThrows(EntityNotFoundException.class,
                () -> service.registerReturn(operator(), new InventoryItem(), 2));
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
        public List<InventoryItem> findByProduct(Product product) {
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
