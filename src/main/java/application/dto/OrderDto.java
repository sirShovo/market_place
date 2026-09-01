package application.dto;

import application.domain.model.valueobject.OrderStatus;

/**
 * Data Transfer Object for Order representation.
 */
public record OrderDto(
        Long id,
        Long buyerId,
        OrderStatus status
) {}
