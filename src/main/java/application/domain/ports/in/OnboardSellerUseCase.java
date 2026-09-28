package application.domain.ports.in;

import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;

/**
 * An {@code ADMIN} registers a seller and its first warehouse in one flow
 * (spec 6.1.1). {@code sellerAccount} carries the {@code username}/{@code password}
 * the seller will use to log in once Phase 6 exists; the service copies identity
 * fields from {@code seller} onto it.
 */
public interface OnboardSellerUseCase {

    Seller onboard(User requester, Seller seller, Warehouse firstWarehouse, User sellerAccount);
}
