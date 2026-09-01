package application.domain.ports.in;

import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;

/** An {@code ADMIN} registers a seller and its first warehouse in one flow (spec 6.1.1). */
public interface OnboardSellerUseCase {

    Seller onboard(User requester, Seller seller, Warehouse firstWarehouse);
}
