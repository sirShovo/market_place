package application.domain.services.seller;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.in.ConsultSellerUseCase;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.services.authorization.ValidateSellerOwnershipService;
import application.domain.services.authorization.ValidateUserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Returns a seller. A {@code SELLER} may only read its own data; {@code ADMIN} /
 * {@code SUPERVISOR} may read any (spec RG03).
 */
@Service
@RequiredArgsConstructor
public class ConsultSellerService implements ConsultSellerUseCase {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;

    @Override
    public Seller consult(User requester, Seller probe) {
        validateUserStatusService.execute(requester);
        validateSellerOwnershipService.execute(requester, probe);
        return sellerRepositoryPort.findByIdentification(probe)
                .orElseThrow(() -> new EntityNotFoundException("Seller"));
    }
}
