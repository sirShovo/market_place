package application.domain.services.buyer;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.ports.in.UpdateBuyerUseCase;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.services.authorization.ValidateBuyerOwnershipService;
import application.domain.services.authorization.ValidateUserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Updates a buyer's contact data and delivery addresses. A buyer may only update its
 * own record (spec RG03). This is not an audited operation.
 */
@Service
@RequiredArgsConstructor
public class UpdateBuyerService implements UpdateBuyerUseCase {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;

    @Override
    public Buyer update(User requester, Buyer buyer) {
        validateUserStatusService.execute(requester);
        validateBuyerOwnershipService.execute(requester, buyer);

        Buyer stored = buyerRepositoryPort.findByIdentification(buyer)
                .orElseThrow(() -> new EntityNotFoundException("Buyer"));

        stored.setFullName(buyer.getFullName());
        stored.setPhoneNumber(buyer.getPhoneNumber());
        stored.setAddress(buyer.getAddress());
        stored.setMainAddress(buyer.getMainAddress());
        stored.setAdditionalAddresses(buyer.getAdditionalAddresses());
        buyerRepositoryPort.update(stored);
        return stored;
    }
}
