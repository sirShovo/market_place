package application.domain.services.user;

import application.domain.enums.AuditSeverity;
import application.domain.exceptions.DuplicateUserException;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.RegisterUserUseCase;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.UserStatus;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Registers a system user. User administration is an {@code ADMIN} function; in
 * particular a {@code SELLER} user can only be created by an {@code ADMIN}
 * (spec Domain 3). Document and e-mail are unique across the platform (spec §11).
 */
@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public User register(User requester, User newUser) {
        validateUserStatusService.execute(requester);
        validateRoleAuthorizationService.execute(requester, UserRole.ADMIN);

        if (userRepositoryPort.existsByIdentification(newUser)) {
            throw new DuplicateUserException("A user with this document already exists.");
        }
        if (userRepositoryPort.existsByEmail(newUser)) {
            throw new DuplicateUserException("A user with this e-mail already exists.");
        }

        newUser.setStatus(UserStatus.ACTIVE);
        newUser.setPassword(passwordServicePort.encrypt(newUser.getPassword()));
        User saved = userRepositoryPort.save(newUser);

        Operation operation = Operation.of(OperationType.USER_REGISTRATION, requester, saved);
        registerOperationAndAuditService.execute(operation, AuditSeverity.INFO, Map.of(
                "username", saved.getUsername(),
                "role", saved.getRole().getCode()));
        return saved;
    }
}
