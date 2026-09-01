package application.domain.valueobjects;

import application.domain.exceptions.DomainException;

/**
 * National / tax identification document of a participant. Immutable; unique across
 * the platform (spec §11).
 */
public record DocumentId(String value) {

    public DocumentId {
        if (value == null || value.isBlank()) {
            throw new DomainException("Document identifier cannot be blank.");
        }
    }
}
