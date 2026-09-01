package application.domain.ports.in;

import application.domain.models.Seller;
import application.domain.models.User;

/** Returns a seller ({@code ADMIN} / {@code SUPERVISOR}). */
public interface ConsultSellerUseCase {

    Seller consult(User requester, Seller probe);
}
