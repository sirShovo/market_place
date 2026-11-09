package application.domain.ports.in;

import application.domain.models.Seller;
import application.domain.models.User;

/** Returns a seller; a {@code SELLER} may only read its own data (spec RG03). */
public interface ConsultSellerUseCase {

    Seller consult(User requester, Seller probe);
}
