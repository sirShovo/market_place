package application.domain.services.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import application.domain.exceptions.InvalidCredentialsException;
import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.support.Fakes.PlainPasswords;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.UserStatus;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginServiceTest {

    private UserRepositoryPort userRepo;
    private LoginService loginService;

    @BeforeEach
    void setUp() {
        userRepo = mock(UserRepositoryPort.class);
        loginService = new LoginService(
                userRepo,
                new PlainPasswords(),
                user -> "JWT-FOR-" + user.getUsername()
        );
    }

    private User activeUser() {
        User u = new User();
        u.setUserId(1);
        u.setUsername("juan");
        u.setPassword("enc:secret123");
        u.setIdentification(new DocumentId("DOC-1"));
        u.setFullName("Juan Perez");
        u.setEmail(new Email("juan@test.com"));
        u.setRole(UserRole.BUYER);
        u.setStatus(UserStatus.ACTIVE);
        return u;
    }

    @Test
    void activeUserWithMatchingPasswordReceivesJwtToken() {
        when(userRepo.findByUsername(any(User.class))).thenReturn(Optional.of(activeUser()));

        String token = loginService.login("juan", "secret123");

        assertEquals("JWT-FOR-juan", token);
    }

    @Test
    void wrongPasswordThrowsInvalidCredentialsException() {
        when(userRepo.findByUsername(any(User.class))).thenReturn(Optional.of(activeUser()));

        assertThrows(InvalidCredentialsException.class,
                () -> loginService.login("juan", "wrong"));
    }

    @Test
    void unknownUsernameThrowsInvalidCredentialsException() {
        when(userRepo.findByUsername(any(User.class))).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> loginService.login("ghost", "secret123"));
    }

    @Test
    void blockedUserCannotLogin() {
        User blocked = activeUser();
        blocked.setStatus(UserStatus.BLOCKED);
        when(userRepo.findByUsername(any(User.class))).thenReturn(Optional.of(blocked));

        assertThrows(InvalidCredentialsException.class,
                () -> loginService.login("juan", "secret123"));
    }

    @Test
    void blankCredentialsThrowInvalidCredentialsException() {
        assertThrows(InvalidCredentialsException.class,
                () -> loginService.login(" ", "secret123"));
        assertThrows(InvalidCredentialsException.class,
                () -> loginService.login("juan", ""));
    }
}
