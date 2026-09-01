package application.domain.services.user;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.User;
import application.domain.ports.in.ConsultUserUseCase;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.valueobjects.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Returns a system user. Restricted to {@code ADMIN} / {@code SUPERVISOR}.
 */
@Service
@RequiredArgsConstructor
public class ConsultUserService implements ConsultUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;

    @Override
    public User consult(User requester, User probe) {
        validateUserStatusService.execute(requester);
        validateRoleAuthorizationService.execute(requester, UserRole.ADMIN, UserRole.SUPERVISOR);
        return userRepositoryPort.findByUsername(probe)
                .orElseThrow(() -> new EntityNotFoundException("User"));
    }
}
