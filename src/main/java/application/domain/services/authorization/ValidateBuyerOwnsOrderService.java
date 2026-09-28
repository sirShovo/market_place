package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Buyer;
import application.domain.models.Order;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * Ensures a buyer only acts on its own order (spec RG03). Used by buyer-driven order
 * services such as {@code ProcessOrderPaymentService}, which take a {@code Buyer}
 * actor rather than a {@code User} requester.
 */
@Service
public class ValidateBuyerOwnsOrderService {

    public void execute(Buyer buyer, Order order) {
        boolean ownOrder = buyer != null && order != null && order.getBuyer() != null
                && Objects.equals(buyer.getIdentification(), order.getBuyer().getIdentification());
        if (!ownOrder) {
            throw new UnauthorizedOperationException("A buyer can only act on its own order.");
        }
    }
}
