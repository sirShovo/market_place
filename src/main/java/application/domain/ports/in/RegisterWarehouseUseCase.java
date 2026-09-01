package application.domain.ports.in;

import application.domain.models.User;
import application.domain.models.Warehouse;

/** An {@code ADMIN} registers an additional warehouse (MARKETPLACE or SELLER). */
public interface RegisterWarehouseUseCase {

    Warehouse register(User requester, Warehouse warehouse);
}
