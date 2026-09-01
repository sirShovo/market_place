package application.domain.ports.in;

import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.User;

/** Reserves stock for an order; rejects non-existent or {@code DAMAGED} inventory (spec §11). */
public interface ReserveInventoryUseCase {

    InventoryMovement reserve(User requester, InventoryItem probe, int quantity);
}
