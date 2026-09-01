package application.domain.models;

import application.domain.valueobjects.UserStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * System identity used for authentication and authorization. The role is inherited
 * from {@link Person}; each user has exactly one role (spec RG02).
 */
@Getter
@Setter
@NoArgsConstructor
public class User extends Person implements AuditableEntity {

    private Integer userId;
    private String username;
    private String password;
    private UserStatus status;

    @Override
    public String auditableType() {
        return "User";
    }

    @Override
    public String auditableId() {
        return username;
    }
}
