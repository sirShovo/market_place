package application.domain.ports.in;

import application.domain.models.InventoryItem;
import application.domain.models.Product;
import application.domain.models.User;
import java.util.List;

/** Returns the stock of a product across warehouses. */
public interface ConsultInventoryUseCase {

    List<InventoryItem> consult(User requester, Product product);
}
