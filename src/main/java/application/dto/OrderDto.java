package application.dto;

import domain.model.valueobject.OrderStatus;

/**
 * Data Transfer Object for Order representation.
 */
public record OrderDto(
        Long id,
        Long buyerId,
        OrderStatus status
) {}
