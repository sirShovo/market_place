package application.usecase;

import application.dto.OrderDto;
import application.port.input.OrderInputPort;
import domain.exception.ResourceNotFoundException;
import domain.model.aggregate.Order;
import domain.repository.OrderRepository;
import domain.service.OrderCheckoutDomainService;

/**
 * Use Case implementation for Order operations.
 */
public class OrderUseCase implements OrderInputPort {

    private final OrderRepository orderRepository;
    private final OrderCheckoutDomainService checkoutDomainService;

    public OrderUseCase(OrderRepository orderRepository, OrderCheckoutDomainService checkoutDomainService) {
        this.orderRepository = orderRepository;
        this.checkoutDomainService = checkoutDomainService;
    }

    @Override
    public OrderDto checkoutOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Delegate to Domain Service for business rules
        checkoutDomainService.initiateCheckout(order);
        
        // For simplicity in this use case, we assume it contains digital products.
        // In a complete flow, this would check the items list to determine physical/digital contents.
        checkoutDomainService.processOrderPayment(order, false);

        Order savedOrder = orderRepository.save(order);

        return new OrderDto(savedOrder.getId(), savedOrder.getBuyerId(), savedOrder.getStatus());
    }
}
