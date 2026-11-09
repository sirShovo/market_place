package application.infrastructure.security;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.ports.out.BuyerRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the authenticated domain {@link User} (or its corresponding {@link Buyer}
 * profile via shared {@code DocumentId}) from the {@link SecurityContextHolder}.
 */
@Component
@RequiredArgsConstructor
public class AuthenticatedUserResolver {

    private final BuyerRepositoryPort buyerRepositoryPort;

    public User resolveUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new UnauthorizedOperationException("No authenticated user found in security context.");
        }
        return user;
    }

    public Buyer resolveBuyer() {
        User user = resolveUser();
        if (user.getIdentification() == null) {
            throw new UnauthorizedOperationException("Authenticated user has no linked identification.");
        }
        Buyer probe = new Buyer();
        probe.setIdentification(user.getIdentification());
        return buyerRepositoryPort.findByIdentification(probe)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Buyer not found for identification: " + user.getIdentification().value()));
    }
}
