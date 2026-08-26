package application.usecase;

import application.dto.UserDto;
import application.port.input.UserInputPort;
import domain.exception.DomainValidationException;
import domain.model.entity.User;
import domain.model.valueobject.DocumentId;
import domain.model.valueobject.Email;
import domain.model.valueobject.UserRole;
import domain.repository.UserRepository;

/**
 * Use Case implementation for User operations.
 * Orchestrates domain logic and interacts with output ports.
 */
public class UserUseCase implements UserInputPort {

    private final UserRepository userRepository;

    /**
     * Constructor for Dependency Injection.
     * @param userRepository The domain output port for User persistence.
     */
    public UserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto registerUser(String fullName, String email, String documentId, UserRole role, UserRole requesterRole) {
        // Business Rule: Sellers cannot self-register, must be onboarded by Admin.
        if (role == UserRole.SELLER && requesterRole != UserRole.ADMIN) {
            throw new DomainValidationException("Only Administrators can register new Sellers.");
        }

        // Domain Value Objects will auto-validate formats
        Email validEmail = new Email(email);
        DocumentId validDocument = new DocumentId(documentId);

        // Create Entity
        User newUser = new User(fullName, validEmail, validDocument, role);

        // Persist using Domain Repository Output Port
        User savedUser = userRepository.save(newUser);

        // Return DTO
        return new UserDto(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail().getValue(),
                savedUser.getDocumentId().getValue(),
                savedUser.getRole(),
                savedUser.getStatus()
        );
    }
}
