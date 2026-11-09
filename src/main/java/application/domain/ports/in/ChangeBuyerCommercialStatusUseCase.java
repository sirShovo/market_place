package application.domain.ports.in;

import application.domain.models.Buyer;
import application.domain.models.User;

/**
 * An {@code ADMIN} activates or suspends a buyer's commercial status (spec Domain 2).
 */
public interface ChangeBuyerCommercialStatusUseCase {

    Buyer changeCommercialStatus(User requester, Buyer target);
}
