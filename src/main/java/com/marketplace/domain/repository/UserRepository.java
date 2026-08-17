package com.marketplace.domain.repository;

import com.marketplace.domain.model.entity.User;
import java.util.Optional;
import java.util.List;

/**
 * Output port interface for User persistence.
 * To be implemented by driven adapters (e.g., JPA adapter).
 */
public interface UserRepository {
    
    /**
     * Saves a user to the repository.
     *
     * @param user The user entity to save.
     * @return The persisted user entity.
     */
    User save(User user);
    
    /**
     * Retrieves a user by their unique identifier.
     *
     * @param id The user's ID.
     * @return An Optional containing the user if found, or empty otherwise.
     */
    Optional<User> findById(Long id);
    
    /**
     * Retrieves a user by their email address.
     *
     * @param email The user's email.
     * @return An Optional containing the user if found, or empty otherwise.
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Retrieves all users in the system.
     *
     * @return A list of all users.
     */
    List<User> findAll();
    
    /**
     * Checks if a user exists with the given email.
     *
     * @param email The email to check.
     * @return true if the email is already in use, false otherwise.
     */
    boolean existsByEmail(String email);
}
