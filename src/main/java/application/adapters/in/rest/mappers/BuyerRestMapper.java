package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.RegisterBuyerRequest;
import application.adapters.in.rest.requests.UpdateBuyerRequest;
import application.adapters.in.rest.responses.BuyerResponse;
import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.valueobjects.BuyerCommercialStatus;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class BuyerRestMapper {

    public Buyer toDomain(RegisterBuyerRequest request) {
        if (request == null) {
            return null;
        }
        Buyer buyer = new Buyer();
        buyer.setIdentification(new DocumentId(request.identification()));
        buyer.setFullName(request.fullName());
        buyer.setEmail(new Email(request.email()));
        buyer.setPhoneNumber(request.phoneNumber());
        buyer.setAddress(request.address());
        buyer.setMainAddress(request.mainAddress());
        buyer.setAdditionalAddresses(
                request.additionalAddresses() != null
                        ? new ArrayList<>(request.additionalAddresses())
                        : new ArrayList<>());
        return buyer;
    }

    public User toAccountDomain(RegisterBuyerRequest request) {
        if (request == null) {
            return null;
        }
        User account = new User();
        account.setUsername(request.username());
        account.setPassword(request.password());
        return account;
    }

    public Buyer toDomain(String identification, UpdateBuyerRequest request) {
        if (request == null) {
            return null;
        }
        Buyer buyer = new Buyer();
        buyer.setIdentification(new DocumentId(identification));
        buyer.setFullName(request.fullName());
        buyer.setEmail(new Email(request.email()));
        buyer.setPhoneNumber(request.phoneNumber());
        buyer.setAddress(request.address());
        buyer.setMainAddress(request.mainAddress());
        buyer.setAdditionalAddresses(
                request.additionalAddresses() != null
                        ? new ArrayList<>(request.additionalAddresses())
                        : new ArrayList<>());
        return buyer;
    }

    public Buyer toStatusTarget(String identification, String statusCode) {
        Buyer target = new Buyer();
        target.setIdentification(new DocumentId(identification));
        target.setCommercialStatus(BuyerCommercialStatus.fromCode(statusCode));
        return target;
    }

    public Buyer toProbe(String identification) {
        Buyer probe = new Buyer();
        probe.setIdentification(new DocumentId(identification));
        return probe;
    }

    public BuyerResponse toResponse(Buyer domain) {
        if (domain == null) {
            return null;
        }
        return new BuyerResponse(
                domain.getIdentification() != null ? domain.getIdentification().value() : null,
                domain.getFullName(),
                domain.getEmail() != null ? domain.getEmail().value() : null,
                domain.getPhoneNumber(),
                domain.getAddress(),
                domain.getCommercialStatus() != null ? domain.getCommercialStatus().getCode() : null,
                domain.getMainAddress(),
                domain.getAdditionalAddresses() != null ? List.copyOf(domain.getAdditionalAddresses()) : List.of()
        );
    }
}
