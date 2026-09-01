package application.port.input;

import application.dto.UserDto;
import application.domain.model.valueobject.UserRole;

/**
 * Input Port for User operations.
 * Defines the contract that the Web Controllers can call.
 */
public interface UserInputPort {
    
    /**
     * Registers a new user in the system.
     * Enforces the business rule that sellers must be registered by an Admin.
     *
     * @param fullName User's full name.
     * @param email User's email.
     * @param documentId User's document ID.
     * @param role User's requested role.
     * @param requesterRole The role of the user requesting the creation.
     * @return The created UserDto.
     */
    UserDto registerUser(String fullName, String email, String documentId, UserRole role, UserRole requesterRole);
}
