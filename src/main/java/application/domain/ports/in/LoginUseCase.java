package application.domain.ports.in;

import application.domain.models.User;

/**
 * Authenticates a user by credentials and returns a signed JWT token.
 */
public interface LoginUseCase {

    String login(User credentials);

    default String login(String username, String rawPassword) {
        User probe = new User();
        probe.setUsername(username);
        probe.setPassword(rawPassword);
        return login(probe);
    }
}
