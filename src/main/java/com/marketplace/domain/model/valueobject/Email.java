package com.marketplace.domain.model.valueobject;

import com.marketplace.domain.exception.DomainValidationException;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representing an email address.
 * 
 * Ensures that the email string format is valid according to standard constraints
 * upon instantiation. As a Value Object, it is immutable.
 */
public class Email {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private final String value;

    /**
     * Constructs an Email instance.
     *
     * @param value The raw email string.
     * @throws DomainValidationException If the email is null, blank, or improperly formatted.
     */
    public Email(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("Email cannot be empty");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new DomainValidationException("Invalid email format");
        }
        this.value = value;
    }

    /**
     * Retrieves the string representation of the email.
     *
     * @return The email string.
     */
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
