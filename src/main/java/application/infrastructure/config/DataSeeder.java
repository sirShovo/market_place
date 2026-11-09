package application.infrastructure.config;

import application.domain.enums.AuditSeverity;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.UserStatus;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Idempotent bootstrap component that ensures an initial {@code ADMIN} user exists
 * on platform startup so that sellers, warehouses, and other system users can be onboarded.
 */
@Slf4j
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;
    private final boolean seedEnabled;
    private final String adminUsername;
    private final String adminPassword;
    private final String adminIdentification;
    private final String adminEmail;
    private final String adminFullName;

    public DataSeeder(
            UserRepositoryPort userRepositoryPort,
            PasswordServicePort passwordServicePort,
            RegisterOperationAndAuditService registerOperationAndAuditService,
            @Value("${app.seed.enabled:true}") boolean seedEnabled,
            @Value("${app.seed.admin.username:admin}") String adminUsername,
            @Value("${app.seed.admin.password:admin123}") String adminPassword,
            @Value("${app.seed.admin.identification:ADMIN-001}") String adminIdentification,
            @Value("${app.seed.admin.email:admin@nexusmarket.com}") String adminEmail,
            @Value("${app.seed.admin.full-name:System Administrator}") String adminFullName) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordServicePort = passwordServicePort;
        this.registerOperationAndAuditService = registerOperationAndAuditService;
        this.seedEnabled = seedEnabled;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.adminIdentification = adminIdentification;
        this.adminEmail = adminEmail;
        this.adminFullName = adminFullName;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }
        try {
            User probe = new User();
            probe.setUsername(adminUsername);
            DocumentId docId = new DocumentId(adminIdentification);
            Email email = new Email(adminEmail);

            if (userRepositoryPort.findByUsername(probe).isPresent()
                    || userRepositoryPort.existsByIdentification(docId)
                    || userRepositoryPort.existsByEmail(email)) {
                log.info("Initial ADMIN user '{}' already exists; skipping seed.", adminUsername);
                return;
            }

            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordServicePort.encrypt(adminPassword));
            admin.setIdentification(docId);
            admin.setFullName(adminFullName);
            admin.setEmail(email);
            admin.setRole(UserRole.ADMIN);
            admin.setStatus(UserStatus.ACTIVE);

            User saved = userRepositoryPort.save(admin);

            Operation operation = Operation.of(OperationType.USER_REGISTRATION, saved, saved);
            registerOperationAndAuditService.execute(
                    operation,
                    AuditSeverity.INFO,
                    Map.of("role", UserRole.ADMIN.getCode(), "bootstrap", true));

            log.info("Bootstrapped initial ADMIN user '{}'.", adminUsername);
        } catch (Exception ex) {
            log.warn("Could not execute initial ADMIN DataSeeder: {}", ex.getMessage());
        }
    }
}
