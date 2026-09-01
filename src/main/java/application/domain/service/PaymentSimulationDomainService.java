package application.domain.service;

import application.domain.model.aggregate.Order;
import application.domain.model.valueobject.OrderStatus;
import application.domain.exception.DomainValidationException;
import application.domain.exception.PaymentRejectedException;
import java.util.Random;

/**
 * Domain Service for simulating payment processing.
 * This satisfies the requirement to randomly reject payments so buyers can retry.
 */
public class PaymentSimulationDomainService {

    private final Random random = new Random();

    /**
     * Attempts to process the payment for an order.
     * Simulates an 80% success rate and 20% failure rate.
     *
     * @param order The order to be paid.
     * @throws DomainValidationException If the order is not in PENDING_PAYMENT state.
     * @throws PaymentRejectedException If the simulated payment fails.
     */
    public void processPayment(Order order) {
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new DomainValidationException("Order must be in PENDING_PAYMENT state to be paid.");
        }

        // Simulate an 80% success rate
        int chance = random.nextInt(100);
        if (chance < 20) {
            // Payment failed (20% chance)
            throw new PaymentRejectedException("Payment was rejected by the simulated gateway. Please try again.");
        }

        // Payment succeeded
        order.setStatus(OrderStatus.PAID);
    }
}
