package application.domain.ports.in;

import application.domain.models.Buyer;
import application.domain.models.User;

/** Updates a buyer's data; a buyer may only update itself (spec RG03). */
public interface UpdateBuyerUseCase {

    Buyer update(User requester, Buyer buyer);
}
