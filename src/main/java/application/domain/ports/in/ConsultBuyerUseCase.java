package application.domain.ports.in;

import application.domain.models.Buyer;
import application.domain.models.User;

/** Returns a buyer; a buyer can only read its own data (spec RG03). */
public interface ConsultBuyerUseCase {

    Buyer consult(User requester, Buyer probe);
}
