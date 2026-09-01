package application.domain.services.buyer;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.ports.in.ConsultBuyerUseCase;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.services.authorization.ValidateBuyerOwnershipService;
import application.domain.services.authorization.ValidateUserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Returns a buyer. A {@code BUYER} may only read its own data; {@code ADMIN} /
 * {@code SUPERVISOR} may read any (spec RG03).
 */
@Service
@RequiredArgsConstructor
public class ConsultBuyerService implements ConsultBuyerUseCase {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;

    @Override
    public Buyer consult(User requester, Buyer probe) {
        validateUserStatusService.execute(requester);
        validateBuyerOwnershipService.execute(requester, probe);
        return buyerRepositoryPort.findByIdentification(probe)
                .orElseThrow(() -> new EntityNotFoundException("Buyer"));
    }
}
