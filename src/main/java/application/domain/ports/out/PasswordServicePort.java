package application.domain.ports.out;

/**
 * Abstracts password hashing and verification. The domain must not depend on a
 * specific hashing library or on Spring Security.
 */
public interface PasswordServicePort {

    String encrypt(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
