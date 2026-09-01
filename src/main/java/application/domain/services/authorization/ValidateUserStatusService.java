package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import application.domain.valueobjects.UserStatus;
import org.springframework.stereotype.Service;

/**
 * Verifies that the requesting user is authenticated and {@code ACTIVE} (spec RG01).
 */
@Service
public class ValidateUserStatusService {

    public void execute(User requester) {
        if (requester == null || !UserStatus.ACTIVE.equals(requester.getStatus())) {
            throw new UnauthorizedOperationException("Requester is not an active user.");
        }
    }
}
