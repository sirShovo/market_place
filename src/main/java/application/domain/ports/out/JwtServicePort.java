package application.domain.ports.out;

import application.domain.models.User;

/**
 * Abstracts JWT generation. The token may carry claims such as {@code username} and
 * {@code role}, but never the user's password.
 */
public interface JwtServicePort {

    String generateToken(User user);
}
