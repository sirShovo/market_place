package application.domain.services.order;

import application.domain.enums.AuditSeverity;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Operation;
import application.domain.models.Order;
import application.domain.models.User;
import application.domain.ports.in.ConfirmDeliveryUseCase;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.UserRole;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * A {@code LOGISTICS_OPERATOR} confirms delivery of a dispatched order
 * ({@code DISPATCHED -> DELIVERED}). The order becomes finalized and immutable
 * (spec §11).
 */
@Service
@RequiredArgsConstructor
public class ConfirmDeliveryService implements ConfirmDeliveryUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public Order confirmDelivery(User requester, Order order) {
        validateUserStatusService.execute(requester);
        validateRoleAuthorizationService.execute(requester, UserRole.LOGISTICS_OPERATOR);

        Order stored = orderRepositoryPort.findByIdentifier(order)
                .orElseThrow(() -> new EntityNotFoundException("Order"));

        stored.transitionTo(OrderStatus.DELIVERED);
        orderRepositoryPort.update(stored);

        Operation operation = Operation.of(OperationType.ORDER_DELIVERY, requester, stored);
        registerOperationAndAuditService.execute(operation, AuditSeverity.INFO, Map.of(
                "order", stored.getIdentifier()));
        return stored;
    }
}
