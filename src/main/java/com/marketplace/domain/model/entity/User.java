package com.marketplace.domain.model.entity;

import com.marketplace.domain.exception.DomainValidationException;
import com.marketplace.domain.model.valueobject.Email;
import com.marketplace.domain.model.valueobject.PhoneNumber;
import com.marketplace.domain.model.valueobject.UserRole;
import com.marketplace.domain.model.valueobject.UserStatus;
import java.time.LocalDateTime;

/**
 * Domain entity representing a User within the Market Place.
 * 
 * Users can have different roles (ADMIN, SELLER, BUYER) which dictate their permissions.
 * The entity manages its own status transitions and validates core data upon creation.
 */
public class User {
    private Long id;
    private String fullName;
    private Email email;
    private PhoneNumber phone;
    private String address;
    private UserRole role;
    private UserStatus status;
    private String passwordHash;
    private LocalDateTime createdAt;

    /**
     * Default constructor for JPA and Mappers. Should not be used directly for domain logic.
     */
    protected User() {}

    private User(String fullName, Email email, PhoneNumber phone, String address, UserRole role) {
        if (fullName == null || fullName.isBlank()) {
            throw new DomainValidationException("Full name cannot be empty");
        }
        if (role == null) {
            throw new DomainValidationException("Role cannot be null");
        }
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.role = role;
        this.status = UserStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Factory method to create a new User.
     *
     * @param fullName The user's full name.
     * @param emailStr The user's email address as a string.
     * @param phoneStr The user's phone number as a string.
     * @param address  The user's physical address.
     * @param role     The assigned role.
     * @return A new User entity instance.
     */
    public static User create(String fullName, String emailStr, String phoneStr, String address, UserRole role) {
        return new User(fullName, new Email(emailStr), new PhoneNumber(phoneStr), address, role);
    }

    /**
     * Blocks the user, preventing them from performing operations.
     */
    public void block() {
        this.status = UserStatus.BLOCKED;
    }

    /**
     * Activates the user, allowing them to perform operations.
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    /**
     * Checks if the user is currently active.
     *
     * @return true if the user's status is ACTIVE, false otherwise.
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFullName() { return fullName; }
    public Email getEmail() { return email; }
    public PhoneNumber getPhone() { return phone; }
    public String getAddress() { return address; }
    public UserRole getRole() { return role; }
    public UserStatus getStatus() { return status; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
}
