package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.valueobjects.UserRole;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * Ensures a {@code SELLER} only accesses its own information; {@code ADMIN} and
 * {@code SUPERVISOR} may access any seller (spec Domain 3, RG03).
 */
@Service
public class ValidateSellerOwnershipService {

    public void execute(User requester, Seller target) {
        if (requester == null || requester.getRole() == null) {
            throw new UnauthorizedOperationException("Missing requester.");
        }
        if (UserRole.ADMIN.equals(requester.getRole()) || UserRole.SUPERVISOR.equals(requester.getRole())) {
            return;
        }
        if (UserRole.SELLER.equals(requester.getRole())
                && target != null
                && Objects.equals(requester.getIdentification(), target.getIdentification())) {
            return;
        }
        throw new UnauthorizedOperationException("A seller can only access its own information.");
    }
}
