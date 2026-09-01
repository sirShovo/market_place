package application.dto;

import application.domain.model.valueobject.UserRole;
import application.domain.model.valueobject.UserStatus;

/**
 * Data Transfer Object for User representation.
 * Prevents exposing the internal Domain Entity outside the application layer.
 */
public record UserDto(
        Long id,
        String fullName,
        String email,
        String documentId,
        UserRole role,
        UserStatus status
) {}
