package application.domain.services.order;

import application.domain.enums.AuditSeverity;
import application.domain.enums.NotificationChannel;
import application.domain.enums.PaymentResult;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.PaymentRejectedException;
import application.domain.models.Buyer;
import application.domain.models.Notification;
import application.domain.models.Operation;
import application.domain.models.Order;
import application.domain.ports.in.ProcessOrderPaymentUseCase;
import application.domain.ports.out.NotificationPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.PaymentGatewayPort;
import application.domain.services.authorization.ValidateBuyerCanPurchaseService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.OrderStatus;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Processes payment for a {@code PENDING_PAYMENT} order through {@link PaymentGatewayPort}.
 * On approval the order becomes {@code PAID}; a digital-only order goes straight to
 * {@code DELIVERED}. On rejection the order stays {@code PENDING_PAYMENT} and the buyer
 * may retry (spec Domain 7, model operative flow step 6).
 */
@Service
@RequiredArgsConstructor
public class ProcessOrderPaymentService implements ProcessOrderPaymentUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final PaymentGatewayPort paymentGatewayPort;
    private final NotificationPort notificationPort;
    private final ValidateBuyerCanPurchaseService validateBuyerCanPurchaseService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public Order pay(Buyer buyer, Order order) {
        validateBuyerCanPurchaseService.execute(buyer);

        Order stored = orderRepositoryPort.findByIdentifier(order)
                .orElseThrow(() -> new EntityNotFoundException("Order"));

        PaymentResult result = paymentGatewayPort.process(stored);
        if (result == PaymentResult.REJECTED) {
            Operation rejected = Operation.of(OperationType.ORDER_PAYMENT, null, stored);
            registerOperationAndAuditService.execute(rejected, AuditSeverity.ERROR, Map.of(
                    "order", stored.getIdentifier(), "result", "REJECTED"));
            throw new PaymentRejectedException("Payment was rejected. Please try again.");
        }

        stored.transitionTo(OrderStatus.PAID);
        if (!stored.containsPhysicalProducts()) {
            stored.transitionTo(OrderStatus.DELIVERED);
        }
        orderRepositoryPort.update(stored);

        Operation approved = Operation.of(OperationType.ORDER_PAYMENT, null, stored);
        registerOperationAndAuditService.execute(approved, AuditSeverity.INFO, Map.of(
                "order", stored.getIdentifier(),
                "result", "APPROVED",
                "status", stored.getStatus().getCode()));

        notifyBuyer(stored);
        return stored;
    }

    private void notifyBuyer(Order order) {
        Notification notification = new Notification();
        notification.setChannel(NotificationChannel.EMAIL);
        notification.setRecipient(order.getBuyer().getEmail() != null ? order.getBuyer().getEmail().value() : null);
        notification.setSubject("Payment confirmed");
        notification.setBody("Your order " + order.getIdentifier() + " is now " + order.getStatus().getCode() + ".");
        notificationPort.send(notification);
    }
}
