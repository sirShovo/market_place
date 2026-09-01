package application.domain.services.seller;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.in.ConsultSellerUseCase;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.valueobjects.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Returns a seller. Restricted to {@code ADMIN} / {@code SUPERVISOR}.
 */
@Service
@RequiredArgsConstructor
public class ConsultSellerService implements ConsultSellerUseCase {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;

    @Override
    public Seller consult(User requester, Seller probe) {
        validateUserStatusService.execute(requester);
        validateRoleAuthorizationService.execute(requester, UserRole.ADMIN, UserRole.SUPERVISOR);
        return sellerRepositoryPort.findByIdentification(probe)
                .orElseThrow(() -> new EntityNotFoundException("Seller"));
    }
}
