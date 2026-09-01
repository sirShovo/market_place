package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.valueobjects.UserRole;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * Ensures a {@code SELLER} only manages its own products; {@code ADMIN} may manage
 * any (spec responsibility matrix §12, RG03).
 */
@Service
public class ValidateProductOwnershipService {

    public void execute(User requester, Product product) {
        if (requester == null || requester.getRole() == null) {
            throw new UnauthorizedOperationException("Missing requester.");
        }
        if (UserRole.ADMIN.equals(requester.getRole())) {
            return;
        }
        boolean ownSellerProduct = UserRole.SELLER.equals(requester.getRole())
                && product.getSeller() != null
                && Objects.equals(requester.getIdentification(), product.getSeller().getIdentification());
        if (!ownSellerProduct) {
            throw new UnauthorizedOperationException("A seller can only manage its own products.");
        }
    }
}
