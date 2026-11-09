package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.RegisterUserRequest;
import application.adapters.in.rest.responses.UserResponse;
import application.domain.models.User;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.UserStatus;
import org.springframework.stereotype.Component;

@Component
public class UserRestMapper {

    public User toDomain(RegisterUserRequest request) {
        if (request == null) {
            return null;
        }
        User user = new User();
        user.setIdentification(new DocumentId(request.identification()));
        user.setFullName(request.fullName());
        user.setEmail(new Email(request.email()));
        user.setPhoneNumber(request.phoneNumber());
        user.setAddress(request.address());
        user.setRole(UserRole.fromCode(request.role()));
        user.setUsername(request.username());
        user.setPassword(request.password());
        return user;
    }

    public User toStatusTarget(String username, String statusCode) {
        User target = new User();
        target.setUsername(username);
        target.setStatus(UserStatus.fromCode(statusCode));
        return target;
    }

    public User toProbe(String username) {
        User probe = new User();
        probe.setUsername(username);
        return probe;
    }

    public UserResponse toResponse(User domain) {
        if (domain == null) {
            return null;
        }
        return new UserResponse(
                domain.getUserId(),
                domain.getUsername(),
                domain.getIdentification() != null ? domain.getIdentification().value() : null,
                domain.getFullName(),
                domain.getEmail() != null ? domain.getEmail().value() : null,
                domain.getPhoneNumber(),
                domain.getAddress(),
                domain.getRole() != null ? domain.getRole().getCode() : null,
                domain.getStatus() != null ? domain.getStatus().getCode() : null
        );
    }
}
