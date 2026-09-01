package application.domain.ports.in;

import application.domain.models.User;

/** Returns a system user, subject to the requester's permissions. */
public interface ConsultUserUseCase {

    User consult(User requester, User probe);
}
