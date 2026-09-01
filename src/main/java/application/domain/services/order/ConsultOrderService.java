package application.domain.services.order;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Order;
import application.domain.models.User;
import application.domain.ports.in.ConsultOrderUseCase;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.authorization.ValidateOrderAccessService;
import application.domain.services.authorization.ValidateUserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Returns an order. A {@code BUYER} only sees its own; {@code ADMIN} /
 * {@code SUPERVISOR} / {@code LOGISTICS_OPERATOR} may see any (spec RG03).
 */
@Service
@RequiredArgsConstructor
public class ConsultOrderService implements ConsultOrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateOrderAccessService validateOrderAccessService;

    @Override
    public Order consult(User requester, Order probe) {
        validateUserStatusService.execute(requester);
        Order stored = orderRepositoryPort.findByIdentifier(probe)
                .orElseThrow(() -> new EntityNotFoundException("Order"));
        validateOrderAccessService.execute(requester, stored);
        return stored;
    }
}
