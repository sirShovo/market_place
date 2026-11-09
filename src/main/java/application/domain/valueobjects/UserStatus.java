package application.domain.valueobjects;

import application.domain.exceptions.DomainException;
import java.util.List;

/**
 * System-access status of a {@code User} (spec Domain 1).
 */
public final class UserStatus extends DomainCatalog {

    public static final UserStatus ACTIVE = new UserStatus(
            "ACTIVE", "Active", "User can access the system normally.");
    public static final UserStatus BLOCKED = new UserStatus(
            "BLOCKED", "Blocked", "User access has been suspended.");
    public static final UserStatus INACTIVE = new UserStatus(
            "INACTIVE", "Inactive", "User exists but cannot operate.");

    private static final List<UserStatus> VALUES = List.of(ACTIVE, BLOCKED, INACTIVE);

    private UserStatus(String code, String name, String description) {
        super(code, name, description);
    }

    public static UserStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        return VALUES.stream()
                .filter(v -> v.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new DomainException("Unknown UserStatus code: " + code));
    }
}
