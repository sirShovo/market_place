package application.domain.services.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.enums.PaymentResult;
import application.domain.exceptions.PaymentRejectedException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Buyer;
import application.domain.models.DigitalProduct;
import application.domain.models.Notification;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.PhysicalProduct;
import application.domain.ports.out.NotificationPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.PaymentGatewayPort;
import application.domain.services.authorization.ValidateBuyerCanPurchaseService;
import application.domain.services.authorization.ValidateBuyerOwnsOrderService;
import application.domain.support.Fakes;
import application.domain.valueobjects.BuyerCommercialStatus;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderStatus;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProcessOrderPaymentServiceTest {

    private FakeOrderRepo orderRepo;
    private FakeGateway gateway;
    private FakeNotifications notifications;
    private ProcessOrderPaymentService service;

    @BeforeEach
    void setUp() {
        orderRepo = new FakeOrderRepo();
        gateway = new FakeGateway();
        notifications = new FakeNotifications();
        service = new ProcessOrderPaymentService(
                orderRepo,
                gateway,
                notifications,
                new ValidateBuyerCanPurchaseService(),
                new ValidateBuyerOwnsOrderService(),
                Fakes.auditService(new Fakes.OperationStore(), new Fakes.AuditStore()));
    }

    private Buyer buyer(String document) {
        Buyer buyer = new Buyer();
        buyer.setIdentification(new DocumentId(document));
        buyer.setEmail(new Email("buyer" + document + "@shop.com"));
        buyer.setCommercialStatus(BuyerCommercialStatus.ACTIVE);
        return buyer;
    }

    private Order digitalOrder(Buyer owner) {
        Order order = new Order();
        order.setIdentifier("ORD-1");
        order.setBuyer(owner);
        OrderItem item = new OrderItem();
        DigitalProduct product = new DigitalProduct();
        product.setIdentifier("P-1");
        product.setPrice(Money.of(new BigDecimal("10.00")));
        item.setProduct(product);
        item.setQuantity(1);
        item.setUnitPrice(product.getPrice());
        order.addItem(item);
        order.transitionTo(OrderStatus.PENDING_PAYMENT);
        return order;
    }

    private Order physicalOrder(Buyer owner) {
        Order order = new Order();
        order.setIdentifier("ORD-2");
        order.setBuyer(owner);
        OrderItem item = new OrderItem();
        PhysicalProduct product = new PhysicalProduct();
        product.setIdentifier("P-2");
        product.setPrice(Money.of(new BigDecimal("10.00")));
        item.setProduct(product);
        item.setQuantity(1);
        item.setUnitPrice(product.getPrice());
        order.addItem(item);
        order.transitionTo(OrderStatus.PENDING_PAYMENT);
        return order;
    }

    @Test
    void approvedDigitalOnlyOrderIsDeliveredAndNotified() {
        Buyer owner = buyer("1");
        Order order = digitalOrder(owner);
        orderRepo.stored = order;
        gateway.result = PaymentResult.APPROVED;

        Order result = service.pay(owner, order);

        assertEquals(OrderStatus.DELIVERED, result.getStatus());
        assertEquals(1, notifications.sent.size());
    }

    @Test
    void approvedPhysicalOrderStaysPaid() {
        Buyer owner = buyer("1");
        Order order = physicalOrder(owner);
        orderRepo.stored = order;
        gateway.result = PaymentResult.APPROVED;

        Order result = service.pay(owner, order);

        assertEquals(OrderStatus.PAID, result.getStatus());
    }

    @Test
    void rejectedPaymentKeepsOrderPendingAndAllowsRetry() {
        Buyer owner = buyer("1");
        Order order = digitalOrder(owner);
        orderRepo.stored = order;
        gateway.result = PaymentResult.REJECTED;

        assertThrows(PaymentRejectedException.class, () -> service.pay(owner, order));
        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());
    }

    @Test
    void aBuyerCannotPayAnotherBuyersOrder() {
        Buyer owner = buyer("1");
        Buyer impostor = buyer("2");
        Order order = digitalOrder(owner);
        orderRepo.stored = order;
        gateway.result = PaymentResult.APPROVED;

        assertThrows(UnauthorizedOperationException.class, () -> service.pay(impostor, order));
        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());
    }

    private static final class FakeOrderRepo implements OrderRepositoryPort {
        Order stored;

        @Override
        public Order save(Order order) {
            stored = order;
            return order;
        }

        @Override
        public Optional<Order> findByIdentifier(Order order) {
            return Optional.ofNullable(stored);
        }

        @Override
        public java.util.List<Order> findByBuyer(Buyer buyer) {
            return stored == null ? java.util.List.of() : java.util.List.of(stored);
        }

        @Override
        public void update(Order order) {
            stored = order;
        }
    }

    private static final class FakeGateway implements PaymentGatewayPort {
        PaymentResult result;

        @Override
        public PaymentResult process(Order order) {
            return result;
        }
    }

    private static final class FakeNotifications implements NotificationPort {
        final java.util.List<Notification> sent = new java.util.ArrayList<>();

        @Override
        public void send(Notification notification) {
            sent.add(notification);
        }
    }
}
