package application.adapters.out.payment;

import application.domain.enums.PaymentResult;
import application.domain.models.Order;
import application.domain.ports.out.PaymentGatewayPort;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Simulated payment processor implementing {@link PaymentGatewayPort}.
 * Approves or rejects payment probabilistically based on a configurable threshold
 * so buyers can exercise payment retry flows.
 */
@Component
public class SimulatedPaymentAdapter implements PaymentGatewayPort {

    private final double approvalProbability;
    private final DoubleSupplier randomSource;

    @Autowired
    public SimulatedPaymentAdapter(
            @Value("${app.payment.approval-probability:0.7}") double approvalProbability) {
        this(approvalProbability, () -> ThreadLocalRandom.current().nextDouble());
    }

    public SimulatedPaymentAdapter(double approvalProbability, DoubleSupplier randomSource) {
        this.approvalProbability = approvalProbability;
        this.randomSource = randomSource;
    }

    @Override
    public PaymentResult process(Order order) {
        if (order == null || order.getTotal() == null) {
            return PaymentResult.REJECTED;
        }
        double draw = randomSource.getAsDouble();
        return draw < approvalProbability ? PaymentResult.APPROVED : PaymentResult.REJECTED;
    }
}
