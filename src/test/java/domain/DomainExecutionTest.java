package domain;

import domain.model.aggregate.Order;
import domain.model.entity.InventoryItem;
import domain.model.entity.InventoryMovement;
import domain.model.entity.OrderItem;
import domain.model.valueobject.Money;
import domain.model.valueobject.MovementType;
import domain.model.valueobject.OrderStatus;
import domain.model.valueobject.StockQuantity;
import domain.service.InventoryDomainService;
import domain.service.OrderCheckoutDomainService;
import domain.service.PaymentSimulationDomainService;
import domain.exception.NegativeStockException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DomainExecutionTest {

    @Test
    void testFullDomainExecutionFlow() {
        System.out.println("=== Starting Domain Execution Test ===");

        // 1. Instantiate Domain Services
        PaymentSimulationDomainService paymentService = new PaymentSimulationDomainService();
        InventoryDomainService inventoryService = new InventoryDomainService();
        OrderCheckoutDomainService checkoutService = new OrderCheckoutDomainService(paymentService, inventoryService);

        // 2. Create an Inventory Item (e.g. Laptop with 10 in stock)
        System.out.println("Creating Inventory...");
        InventoryItem laptopInventory = new InventoryItem(1L, 1L, StockQuantity.of(10));
        assertEquals(10, laptopInventory.getStock().getValue());

        // 3. Create an Order
        System.out.println("Creating Order in CART...");
        Order order = new Order(100L);
        order.addItem(new OrderItem(1L, 2, Money.of(new BigDecimal("1500.00"))));
        assertEquals(OrderStatus.CART, order.getStatus());

        // 4. Initiate Checkout
        System.out.println("Initiating Checkout...");
        checkoutService.initiateCheckout(order);
        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());

        // 5. Reserve Inventory
        System.out.println("Reserving 2 Laptops...");
        InventoryMovement movement = inventoryService.reserveStock(laptopInventory, 2);
        
        // Verify stock decreased to 8 and movement was generated
        assertEquals(8, laptopInventory.getStock().getValue());
        assertEquals(MovementType.RESERVE, movement.getType());

        // 6. Validate Negative Stock Business Rule
        System.out.println("Attempting to over-purchase stock (should fail)...");
        assertThrows(NegativeStockException.class, () -> {
            inventoryService.reserveStock(laptopInventory, 10);
        });

        // 7. Simulate Payment (20% random failure chance)
        System.out.println("Processing Payment...");
        boolean paymentSuccess = false;
        int attempts = 0;
        
        while (!paymentSuccess && attempts < 5) {
            attempts++;
            try {
                checkoutService.processOrderPayment(order, false); // false = digital product, transitions to DELIVERED
                paymentSuccess = true;
                System.out.println("Payment successful on attempt " + attempts);
            } catch (Exception e) {
                System.out.println("Payment rejected (Simulation). Retrying...");
            }
        }

        // If payment was successful and digital, status must be DELIVERED
        assertTrue(paymentSuccess);
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        System.out.println("=== Test Successfully Completed ===");
    }
}
