package application.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.exceptions.FinalizedOrderModificationException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderTest {

    private OrderItem digitalLine(String price, int qty) {
        DigitalProduct product = new DigitalProduct();
        product.setIdentifier("P-" + price);
        product.setPrice(Money.of(new BigDecimal(price)));
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(qty);
        item.setUnitPrice(product.getPrice());
        return item;
    }

    @Test
    void totalIsSumOfSubtotals() {
        Order order = new Order();
        order.addItem(digitalLine("10.00", 2));
        order.addItem(digitalLine("5.50", 4));
        assertEquals(new BigDecimal("42.00"), order.getTotal().amount());
    }

    @Test
    void followsTheAllowedLifecycle() {
        Order order = new Order();
        order.addItem(digitalLine("10.00", 1));
        order.transitionTo(OrderStatus.PENDING_PAYMENT);
        order.transitionTo(OrderStatus.PAID);
        order.transitionTo(OrderStatus.DELIVERED);
        assertTrue(order.isFinalized());
    }

    @Test
    void rejectsAnIllegalTransition() {
        Order order = new Order();
        assertThrows(InvalidStatusTransitionException.class,
                () -> order.transitionTo(OrderStatus.PAID));
    }

    @Test
    void finalizedOrderIsImmutable() {
        Order order = new Order();
        order.addItem(digitalLine("10.00", 1));
        order.transitionTo(OrderStatus.PENDING_PAYMENT);
        order.transitionTo(OrderStatus.PAID);
        order.transitionTo(OrderStatus.DELIVERED);
        assertThrows(FinalizedOrderModificationException.class,
                () -> order.addItem(digitalLine("1.00", 1)));
        assertThrows(FinalizedOrderModificationException.class,
                () -> order.transitionTo(OrderStatus.DISPATCHED));
    }
}
