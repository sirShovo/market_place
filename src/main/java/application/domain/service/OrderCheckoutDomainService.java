package application.domain.service;

import application.domain.model.aggregate.Order;
import application.domain.model.aggregate.Product;
import application.domain.model.entity.InventoryItem;
import application.domain.model.valueobject.OrderStatus;
import application.domain.model.valueobject.ProductType;
import application.domain.exception.DomainValidationException;

/**
 * Domain Service that orchestrates the checkout process for an Order.
 * It manages transitions from CART to PAID or SHIPPED depending on the product types.
 */
public class OrderCheckoutDomainService {

    private final PaymentSimulationDomainService paymentService;
    private final InventoryDomainService inventoryService;

    /**
     * Constructs the OrderCheckoutDomainService.
     *
     * @param paymentService   The payment simulation service.
     * @param inventoryService The inventory management service.
     */
    public OrderCheckoutDomainService(PaymentSimulationDomainService paymentService, InventoryDomainService inventoryService) {
        this.paymentService = paymentService;
        this.inventoryService = inventoryService;
    }

    /**
     * Initiates the checkout process for an order, changing its status to PENDING_PAYMENT.
     *
     * @param order The order to checkout.
     * @throws DomainValidationException if the order is empty or not in CART state.
     */
    public void initiateCheckout(Order order) {
        if (order.getStatus() != OrderStatus.CART) {
            throw new DomainValidationException("Order must be in CART state to checkout.");
        }
        if (order.getItems().isEmpty()) {
            throw new DomainValidationException("Cannot checkout an empty order.");
        }
        order.setStatus(OrderStatus.PENDING_PAYMENT);
    }

    /**
     * Attempts to pay the order and processes post-payment logic (like physical vs digital).
     *
     * @param order The order to pay.
     * @param containsPhysicalProducts True if the order contains at least one physical product.
     */
    public void processOrderPayment(Order order, boolean containsPhysicalProducts) {
        // This will throw PaymentRejectedException if simulation fails
        paymentService.processPayment(order);

        // If payment succeeds (no exception thrown), order is now PAID.
        // If the order is purely DIGITAL, we can transition it directly to DELIVERED/FINALIZED.
        if (!containsPhysicalProducts) {
            order.setStatus(OrderStatus.DELIVERED);
        }
        // If it contains physical products, it stays as PAID and awaits logistics to mark as SHIPPED.
    }
}
