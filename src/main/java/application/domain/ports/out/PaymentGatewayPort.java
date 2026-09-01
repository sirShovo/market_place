package application.domain.ports.out;

import application.domain.enums.PaymentResult;
import application.domain.models.Order;

/**
 * Abstracts the payment provider. The simulation (probabilistic approval / rejection
 * that lets a buyer retry) belongs to the adapter, never to the domain.
 */
public interface PaymentGatewayPort {

    PaymentResult process(Order order);
}
