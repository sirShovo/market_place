package application.domain.repository;
import application.domain.model.entity.User;
import java.util.Optional;

/**
 * Output port interface for User persistence.
 */
public interface UserRepository { 
    /**
     * Saves a user to the repository.
     * @param user The user entity to save.
     * @return The persisted user entity.
     */
    User save(User user); 
    
    /**
     * Retrieves a user by their unique identifier.
     * @param id The user's ID.
     * @return An Optional containing the user if found.
     */
    Optional<User> findById(Long id); 
}
