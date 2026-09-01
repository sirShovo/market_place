package application.domain.ports.in;

import application.domain.models.Buyer;

/** Self-service buyer registration (spec Domain 2). */
public interface RegisterBuyerUseCase {

    Buyer register(Buyer buyer);
}
