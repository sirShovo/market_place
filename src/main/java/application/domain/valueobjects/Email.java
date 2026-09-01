package application.domain.valueobjects;

import application.domain.exceptions.DomainException;

/**
 * A syntactically valid e-mail address. Immutable.
 */
public record Email(String value) {

    private static final String PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public Email {
        if (value == null || !value.matches(PATTERN)) {
            throw new DomainException("Invalid email format.");
        }
    }
}
