package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import application.domain.valueobjects.UserRole;
import java.util.Arrays;
import org.springframework.stereotype.Service;

/**
 * Verifies that the requesting user holds one of the roles allowed for an operation
 * (spec responsibility matrix §12).
 */
@Service
public class ValidateRoleAuthorizationService {

    public void execute(User requester, UserRole... allowedRoles) {
        if (requester == null || requester.getRole() == null
                || Arrays.stream(allowedRoles).noneMatch(role -> role.equals(requester.getRole()))) {
            throw new UnauthorizedOperationException(
                    "Operation not allowed for the requester's role.");
        }
    }
}
