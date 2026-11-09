package application.domain.services.order;

import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.User;
import application.domain.ports.in.ConsultBuyerOrdersUseCase;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.authorization.ValidateBuyerOwnershipService;
import application.domain.services.authorization.ValidateUserStatusService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Returns all orders placed by a buyer. A {@code BUYER} only sees its own orders;
 * {@code ADMIN} and {@code SUPERVISOR} may list any buyer's orders (spec RG03).
 */
@Service
@RequiredArgsConstructor
public class ConsultBuyerOrdersService implements ConsultBuyerOrdersUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;

    @Override
    public List<Order> consultByBuyer(User requester, Buyer buyer) {
        validateUserStatusService.execute(requester);
        validateBuyerOwnershipService.execute(requester, buyer);
        return orderRepositoryPort.findByBuyer(buyer);
    }
}
