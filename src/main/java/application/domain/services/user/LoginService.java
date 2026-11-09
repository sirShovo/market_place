package application.domain.services.user;

import application.domain.exceptions.InvalidCredentialsException;
import application.domain.models.User;
import application.domain.ports.in.LoginUseCase;
import application.domain.ports.out.JwtServicePort;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Verifies user credentials and active status, issuing a JWT token upon success.
 */
@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final JwtServicePort jwtServicePort;

    @Override
    public String login(User credentials) {
        if (credentials == null
                || credentials.getUsername() == null || credentials.getUsername().isBlank()
                || credentials.getPassword() == null || credentials.getPassword().isBlank()) {
            throw new InvalidCredentialsException();
        }

        User user = userRepositoryPort.findByUsername(credentials)
                .orElseThrow(InvalidCredentialsException::new);

        if (!UserStatus.ACTIVE.equals(user.getStatus())) {
            throw new InvalidCredentialsException("User account is not active.");
        }

        if (!passwordServicePort.matches(credentials.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return jwtServicePort.generateToken(user);
    }
}
