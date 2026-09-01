package application.domain.ports.in;

import application.domain.models.InventoryItem;
import application.domain.models.User;

/** Records an {@code ENTRY} movement, increasing stock in a warehouse. */
public interface RegisterInventoryEntryUseCase {

    InventoryItem registerEntry(User requester, InventoryItem item, int quantity);
}
