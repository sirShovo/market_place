package application.domain.services.identity;

import application.domain.exceptions.DuplicateUserException;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Enforces that a document identifier and an e-mail address are unique across the
 * whole platform (spec §11) — not just within one participant type. {@code User},
 * {@code Buyer} and {@code Seller} are independent aggregates, so this collaborator
 * checks all three repositories before any of the registration services persists a
 * new participant.
 */
@Service
@RequiredArgsConstructor
public class ValidatePlatformUniquenessService {

    private final UserRepositoryPort userRepositoryPort;
    private final BuyerRepositoryPort buyerRepositoryPort;
    private final SellerRepositoryPort sellerRepositoryPort;

    public void execute(DocumentId identification, Email email) {
        if (userRepositoryPort.existsByIdentification(identification)
                || buyerRepositoryPort.existsByIdentification(identification)
                || sellerRepositoryPort.existsByIdentification(identification)) {
            throw new DuplicateUserException("A participant with this document already exists.");
        }
        if (userRepositoryPort.existsByEmail(email)
                || buyerRepositoryPort.existsByEmail(email)
                || sellerRepositoryPort.existsByEmail(email)) {
            throw new DuplicateUserException("A participant with this e-mail already exists.");
        }
    }
}
