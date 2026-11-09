package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.RegisterWarehouseRequest;
import application.adapters.in.rest.responses.WarehouseResponse;
import application.domain.models.Seller;
import application.domain.models.Warehouse;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.WarehouseType;
import org.springframework.stereotype.Component;

@Component
public class WarehouseRestMapper {

    public Warehouse toDomain(RegisterWarehouseRequest request) {
        if (request == null) {
            return null;
        }
        Warehouse warehouse = new Warehouse();
        warehouse.setIdentifier(request.identifier());
        warehouse.setName(request.name());
        warehouse.setType(WarehouseType.fromCode(request.type()));
        if (request.ownerIdentification() != null && !request.ownerIdentification().isBlank()) {
            Seller owner = new Seller();
            owner.setIdentification(new DocumentId(request.ownerIdentification()));
            warehouse.setOwner(owner);
        }
        warehouse.setLocation(request.location());
        return warehouse;
    }

    public Warehouse toProbe(String identifier) {
        Warehouse probe = new Warehouse();
        probe.setIdentifier(identifier);
        return probe;
    }

    public WarehouseResponse toResponse(Warehouse domain) {
        if (domain == null) {
            return null;
        }
        return new WarehouseResponse(
                domain.getIdentifier(),
                domain.getName(),
                domain.getType() != null ? domain.getType().getCode() : null,
                domain.getOwner() != null && domain.getOwner().getIdentification() != null
                        ? domain.getOwner().getIdentification().value()
                        : null,
                domain.getLocation()
        );
    }
}
