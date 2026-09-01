package application.domain.ports.in;

import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.User;

/** Manually corrects stock to a new quantity ({@code ADJUSTMENT} movement). */
public interface AdjustInventoryUseCase {

    InventoryMovement adjust(User requester, InventoryItem probe, int newQuantity);
}
