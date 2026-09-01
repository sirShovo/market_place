package application.domain.ports.in;

import application.domain.models.User;
import application.domain.models.Warehouse;

/** Returns a warehouse. */
public interface ConsultWarehouseUseCase {

    Warehouse consult(User requester, Warehouse probe);
}
