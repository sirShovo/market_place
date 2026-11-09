package application.adapters.out.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import application.domain.enums.PaymentResult;
import application.domain.models.Order;
import application.domain.valueobjects.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class SimulatedPaymentAdapterTest {

    @Test
    void approvesWhenRandomDrawIsBelowThreshold() {
        SimulatedPaymentAdapter adapter = new SimulatedPaymentAdapter(0.7, () -> 0.5);
        Order order = new Order();
        order.setIdentifier("ORD-1");
        order.setTotal(Money.of(BigDecimal.valueOf(50)));

        assertEquals(PaymentResult.APPROVED, adapter.process(order));
    }

    @Test
    void rejectsWhenRandomDrawIsAtOrAboveThreshold() {
        SimulatedPaymentAdapter adapter = new SimulatedPaymentAdapter(0.7, () -> 0.7);
        Order order = new Order();
        order.setIdentifier("ORD-1");
        order.setTotal(Money.of(BigDecimal.valueOf(50)));

        assertEquals(PaymentResult.REJECTED, adapter.process(order));
    }

    @Test
    void rejectsWhenOrderIsNull() {
        SimulatedPaymentAdapter adapter = new SimulatedPaymentAdapter(1.0, () -> 0.0);
        assertEquals(PaymentResult.REJECTED, adapter.process(null));
    }
}
