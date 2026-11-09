package application.domain.ports.in;

import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.User;

/**
 * Records stock returned to a warehouse ({@code RETURN} movement, spec Domain 6).
 */
public interface RegisterInventoryReturnUseCase {

    InventoryMovement registerReturn(User requester, InventoryItem probe, int quantity);
}
