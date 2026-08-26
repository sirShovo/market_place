package application.port.input;

import application.dto.OrderDto;

/**
 * Input Port for Order and checkout operations.
 */
public interface OrderInputPort {

    /**
     * Executes the checkout process for an order, including payment and inventory updates.
     *
     * @param orderId The ID of the order to checkout.
     * @return The updated OrderDto reflecting the payment status.
     */
    OrderDto checkoutOrder(Long orderId);
}
