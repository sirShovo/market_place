package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.valueobjects.UserRole;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * Ensures a {@code BUYER} only accesses its own information; {@code ADMIN} and
 * {@code SUPERVISOR} may access any buyer (spec Domain 2, RG03).
 */
@Service
public class ValidateBuyerOwnershipService {

    public void execute(User requester, Buyer target) {
        if (requester == null || requester.getRole() == null) {
            throw new UnauthorizedOperationException("Missing requester.");
        }
        if (UserRole.ADMIN.equals(requester.getRole()) || UserRole.SUPERVISOR.equals(requester.getRole())) {
            return;
        }
        if (UserRole.BUYER.equals(requester.getRole())
                && Objects.equals(requester.getIdentification(), target.getIdentification())) {
            return;
        }
        throw new UnauthorizedOperationException("A buyer can only access its own information.");
    }
}
