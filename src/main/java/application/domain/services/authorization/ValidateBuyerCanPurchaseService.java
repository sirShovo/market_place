package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Buyer;
import application.domain.valueobjects.BuyerCommercialStatus;
import org.springframework.stereotype.Service;

/**
 * Ensures a buyer is commercially {@code ACTIVE} before it can add to cart or check out
 * (spec Domain 2: commercial status governs the ability to purchase).
 */
@Service
public class ValidateBuyerCanPurchaseService {

    public void execute(Buyer buyer) {
        if (buyer == null || !BuyerCommercialStatus.ACTIVE.equals(buyer.getCommercialStatus())) {
            throw new UnauthorizedOperationException("Buyer is not allowed to purchase.");
        }
    }
}
