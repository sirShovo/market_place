package application.domain.services.catalog;

import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.in.ConsultSellerProductsUseCase;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.services.authorization.ValidateSellerOwnershipService;
import application.domain.services.authorization.ValidateUserStatusService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Returns all products belonging to a seller. A {@code SELLER} may only list its own
 * portfolio; {@code ADMIN} and {@code SUPERVISOR} may list any seller's (spec RG03).
 */
@Service
@RequiredArgsConstructor
public class ConsultSellerProductsService implements ConsultSellerProductsUseCase {

    private final ProductRepositoryPort productRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;

    @Override
    public List<Product> consultBySeller(User requester, Seller seller) {
        validateUserStatusService.execute(requester);
        validateSellerOwnershipService.execute(requester, seller);
        return productRepositoryPort.findBySeller(seller);
    }
}
