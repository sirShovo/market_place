package application.domain.services.order;

import application.domain.enums.AuditSeverity;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Cart;
import application.domain.models.CartItem;
import application.domain.models.Operation;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.ports.in.CheckoutCartUseCase;
import application.domain.ports.out.CartRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.authorization.ValidateBuyerCanPurchaseService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.CartStatus;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.OrderStatus;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Converts the buyer's active cart into an {@code Order}
 * ({@code CART -> PENDING_PAYMENT}), capturing each line's unit price at checkout time
 * (spec Domain 7).
 */
@Service
@RequiredArgsConstructor
public class CheckoutCartService implements CheckoutCartUseCase {

    private final CartRepositoryPort cartRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final ValidateBuyerCanPurchaseService validateBuyerCanPurchaseService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public Order checkout(Buyer buyer) {
        validateBuyerCanPurchaseService.execute(buyer);

        Cart cart = cartRepositoryPort.findActiveByBuyer(buyer)
                .orElseThrow(() -> new EntityNotFoundException("Active cart"));
        if (cart.getItems().isEmpty()) {
            throw new DomainException("Cannot checkout an empty cart.");
        }

        Order order = new Order();
        order.setIdentifier(UUID.randomUUID().toString());
        order.setBuyer(buyer);
        order.setCreatedAt(LocalDateTime.now());
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setVariant(cartItem.getVariant());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getProduct().getPrice());
            order.addItem(orderItem);
        }
        order.transitionTo(OrderStatus.PENDING_PAYMENT);
        Order saved = orderRepositoryPort.save(order);

        cart.setStatus(CartStatus.CONVERTED);
        cartRepositoryPort.update(cart);

        Operation operation = Operation.of(OperationType.CART_CHECKOUT, null, saved);
        registerOperationAndAuditService.execute(operation, AuditSeverity.INFO, Map.of(
                "order", saved.getIdentifier(),
                "lines", saved.getItems().size(),
                "total", saved.getTotal().amount()));
        return saved;
    }
}
