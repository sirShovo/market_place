package com.marketplace.domain.repository;

import com.marketplace.domain.model.aggregate.Order;
import com.marketplace.domain.model.valueobject.OrderStatus;
import java.util.Optional;
import java.util.List;

/**
 * Output port interface for Order persistence.
 */
public interface OrderRepository {
    
    /**
     * Saves an order to the repository.
     *
     * @param order The order aggregate to save.
     * @return The persisted order aggregate.
     */
    Order save(Order order);
    
    /**
     * Retrieves an order by its unique identifier.
     *
     * @param id The order's ID.
     * @return An Optional containing the order if found, or empty otherwise.
     */
    Optional<Order> findById(Long id);
    
    /**
     * Retrieves all orders placed by a specific buyer.
     *
     * @param buyerId The ID of the buyer.
     * @return A list of orders associated with the buyer.
     */
    List<Order> findByBuyerId(Long buyerId);
    
    /**
     * Retrieves all orders currently in a specific status.
     * Used for batch processing or administrative monitoring (e.g., finding PENDING_PAYMENT orders).
     *
     * @param status The status to filter by.
     * @return A list of orders in the specified status.
     */
    List<Order> findByStatus(OrderStatus status);
}
