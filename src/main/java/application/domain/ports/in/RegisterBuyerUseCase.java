package application.domain.ports.in;

import application.domain.models.Buyer;
import application.domain.models.User;

/**
 * Self-service buyer registration (spec Domain 2). {@code account} carries the
 * {@code username}/{@code password} the buyer will use to log in once Phase 6 exists;
 * the service copies identity fields from {@code buyer} onto it.
 */
public interface RegisterBuyerUseCase {

    Buyer register(Buyer buyer, User account);
}
