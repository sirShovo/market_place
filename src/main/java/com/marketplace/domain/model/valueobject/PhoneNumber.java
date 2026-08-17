package com.marketplace.domain.model.valueobject;

import com.marketplace.domain.exception.DomainValidationException;
import java.util.Objects;

/**
 * Value Object representing a phone number.
 * 
 * Validates the basic structure of a phone number (e.g., optional '+' prefix and numeric body)
 * upon creation. It is immutable.
 */
public class PhoneNumber {
    private final String value;

    /**
     * Constructs a PhoneNumber instance.
     *
     * @param value The raw phone number string.
     * @throws DomainValidationException If the phone number is null, blank, or improperly formatted.
     */
    public PhoneNumber(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("Phone number cannot be empty");
        }
        if (!value.matches("^\\+?[0-9]{7,15}$")) {
            throw new DomainValidationException("Invalid phone number format");
        }
        this.value = value;
    }

    /**
     * Retrieves the string representation of the phone number.
     *
     * @return The phone number string.
     */
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneNumber that = (PhoneNumber) o;
        return Objects.equals(value, that.value);
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
