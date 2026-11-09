package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.OnboardSellerRequest;
import application.adapters.in.rest.responses.SellerResponse;
import application.adapters.in.rest.responses.WarehouseResponse;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SellerRestMapper {

    private final WarehouseRestMapper warehouseRestMapper;

    public Seller toSellerDomain(OnboardSellerRequest request) {
        if (request == null) {
            return null;
        }
        Seller seller = new Seller();
        seller.setIdentification(new DocumentId(request.identification()));
        seller.setFullName(request.fullName());
        seller.setEmail(new Email(request.email()));
        seller.setPhoneNumber(request.phoneNumber());
        seller.setAddress(request.address());
        return seller;
    }

    public Warehouse toFirstWarehouseDomain(OnboardSellerRequest request) {
        if (request == null) {
            return null;
        }
        Warehouse warehouse = new Warehouse();
        warehouse.setIdentifier(request.warehouseIdentifier());
        warehouse.setName(request.warehouseName());
        warehouse.setLocation(request.warehouseLocation());
        return warehouse;
    }

    public User toAccountDomain(OnboardSellerRequest request) {
        if (request == null) {
            return null;
        }
        User account = new User();
        account.setUsername(request.username());
        account.setPassword(request.password());
        return account;
    }

    public Seller toProbe(String identification) {
        Seller probe = new Seller();
        probe.setIdentification(new DocumentId(identification));
        return probe;
    }

    public SellerResponse toResponse(Seller domain) {
        if (domain == null) {
            return null;
        }
        List<WarehouseResponse> warehouses = domain.getWarehouses() != null
                ? domain.getWarehouses().stream().map(warehouseRestMapper::toResponse).toList()
                : List.of();

        return new SellerResponse(
                domain.getIdentification() != null ? domain.getIdentification().value() : null,
                domain.getFullName(),
                domain.getEmail() != null ? domain.getEmail().value() : null,
                domain.getPhoneNumber(),
                domain.getAddress(),
                domain.getStatus() != null ? domain.getStatus().getCode() : null,
                warehouses
        );
    }
}
