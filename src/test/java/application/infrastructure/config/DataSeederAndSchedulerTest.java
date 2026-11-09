package application.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.adapters.in.scheduler.OrderExpirationScheduler;
import application.domain.models.User;
import application.domain.ports.in.ReleaseExpiredOrdersUseCase;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.support.Fakes;
import application.domain.support.Fakes.AuditStore;
import application.domain.support.Fakes.OperationStore;
import application.domain.support.Fakes.PlainPasswords;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DataSeederAndSchedulerTest {

    @Test
    void dataSeederCreatesAdminWhenNotPresentAndSkipsWhenAlreadyPresent() {
        UserRepositoryPort userRepo = mock(UserRepositoryPort.class);
        OperationStore ops = new OperationStore();
        AuditStore audits = new AuditStore();

        when(userRepo.findByUsername(any(User.class))).thenReturn(Optional.empty());
        when(userRepo.existsByIdentification(any())).thenReturn(false);
        when(userRepo.existsByEmail(any())).thenReturn(false);
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        DataSeeder seeder = new DataSeeder(
                userRepo,
                new PlainPasswords(),
                Fakes.auditService(ops, audits),
                true,
                "admin",
                "admin123",
                "ADMIN-001",
                "admin@nexusmarket.com",
                "System Administrator");

        seeder.run();

        verify(userRepo).save(any(User.class));
        assertEquals(1, ops.saved.size());
        assertEquals(1, audits.saved.size());

        User existing = new User();
        existing.setUsername("admin");
        when(userRepo.findByUsername(any(User.class))).thenReturn(Optional.of(existing));

        seeder.run();
        assertEquals(1, ops.saved.size());
    }

    @Test
    void dataSeederDoesNothingWhenDisabled() {
        UserRepositoryPort userRepo = mock(UserRepositoryPort.class);
        DataSeeder seeder = new DataSeeder(
                userRepo,
                new PlainPasswords(),
                Fakes.auditService(new OperationStore(), new AuditStore()),
                false,
                "admin",
                "admin123",
                "ADMIN-001",
                "admin@nexusmarket.com",
                "System Administrator");

        seeder.run();
        verify(userRepo, never()).save(any());
    }

    @Test
    void orderExpirationSchedulerInvokesReleaseExpiredOrdersUseCase() {
        ReleaseExpiredOrdersUseCase useCase = mock(ReleaseExpiredOrdersUseCase.class);
        when(useCase.releaseExpiredOrders()).thenReturn(2);

        OrderExpirationScheduler scheduler = new OrderExpirationScheduler(useCase);
        scheduler.runOrderExpirationCheck();

        verify(useCase).releaseExpiredOrders();
    }
}
