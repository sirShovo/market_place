package application.domain.ports.in;

import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.User;
import java.util.List;

/**
 * Lists all products belonging to a seller; a {@code SELLER} may only list its own
 * portfolio (spec RG03).
 */
public interface ConsultSellerProductsUseCase {

    List<Product> consultBySeller(User requester, Seller seller);
}
