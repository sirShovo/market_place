package application.domain.ports.in;

import application.domain.models.User;

/** Registers a new system user. A {@code SELLER} user may only be created by an {@code ADMIN}. */
public interface RegisterUserUseCase {

    User register(User requester, User newUser);
}
