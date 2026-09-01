package application.domain.ports.in;

import application.domain.models.User;

/** Activates, blocks or deactivates a system user. */
public interface ChangeUserStatusUseCase {

    User changeStatus(User requester, User target);
}
