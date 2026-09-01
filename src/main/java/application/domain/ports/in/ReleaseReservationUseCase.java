package application.domain.ports.in;

import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.User;

/** Returns previously reserved stock to available inventory. */
public interface ReleaseReservationUseCase {

    InventoryMovement release(User requester, InventoryItem probe, int quantity);
}
