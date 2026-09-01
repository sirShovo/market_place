package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Order;
import application.domain.models.User;
import application.domain.valueobjects.UserRole;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * Ensures a {@code BUYER} only accesses its own orders; {@code ADMIN},
 * {@code SUPERVISOR} and {@code LOGISTICS_OPERATOR} may access any (spec §12, RG03).
 */
@Service
public class ValidateOrderAccessService {

    public void execute(User requester, Order order) {
        if (requester == null || requester.getRole() == null) {
            throw new UnauthorizedOperationException("Missing requester.");
        }
        if (UserRole.ADMIN.equals(requester.getRole())
                || UserRole.SUPERVISOR.equals(requester.getRole())
                || UserRole.LOGISTICS_OPERATOR.equals(requester.getRole())) {
            return;
        }
        boolean ownOrder = UserRole.BUYER.equals(requester.getRole())
                && order.getBuyer() != null
                && Objects.equals(requester.getIdentification(), order.getBuyer().getIdentification());
        if (!ownOrder) {
            throw new UnauthorizedOperationException("A buyer can only access its own orders.");
        }
    }
}
