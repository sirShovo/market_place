package application.domain.models;

import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.UserRole;
import lombok.Getter;
import lombok.Setter;

/**
 * Common identity and contact information shared by every marketplace participant
 * (spec Domain 1). Cannot be instantiated directly.
 */
@Getter
@Setter
public abstract class Person {

    private DocumentId identification;
    private String fullName;
    private Email email;
    private String phoneNumber;
    private String address;
    private UserRole role;
}
