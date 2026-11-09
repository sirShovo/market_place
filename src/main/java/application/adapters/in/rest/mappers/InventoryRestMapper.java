package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.InventoryOperationRequest;
import application.adapters.in.rest.responses.InventoryItemResponse;
import application.adapters.in.rest.responses.InventoryMovementResponse;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.PhysicalProduct;
import application.domain.models.Product;
import application.domain.models.Warehouse;
import application.domain.valueobjects.InventoryItemCondition;
import org.springframework.stereotype.Component;

@Component
public class InventoryRestMapper {

    public InventoryItem toProbe(InventoryOperationRequest request) {
        if (request == null) {
            return null;
        }
        Product product = new PhysicalProduct();
        product.setIdentifier(request.productIdentifier());

        Warehouse warehouse = new Warehouse();
        warehouse.setIdentifier(request.warehouseIdentifier());

        InventoryItem probe = new InventoryItem();
        probe.setProduct(product);
        probe.setWarehouse(warehouse);
        if (request.condition() != null && !request.condition().isBlank()) {
            probe.setCondition(InventoryItemCondition.fromCode(request.condition()));
        } else {
            probe.setCondition(InventoryItemCondition.AVAILABLE);
        }
        return probe;
    }

    public InventoryItemResponse toItemResponse(InventoryItem domain) {
        if (domain == null) {
            return null;
        }
        return new InventoryItemResponse(
                domain.getId(),
                domain.getProduct() != null ? domain.getProduct().getIdentifier() : null,
                domain.getWarehouse() != null ? domain.getWarehouse().getIdentifier() : null,
                domain.getStock() != null ? domain.getStock().value() : 0,
                domain.getCondition() != null ? domain.getCondition().getCode() : null
        );
    }

    public InventoryMovementResponse toMovementResponse(InventoryMovement domain) {
        if (domain == null) {
            return null;
        }
        return new InventoryMovementResponse(
                domain.getId(),
                domain.getInventoryItem() != null ? domain.getInventoryItem().getId() : null,
                domain.getType() != null ? domain.getType().getCode() : null,
                domain.getQuantity(),
                domain.getOccurredOn(),
                domain.getPerformedBy() != null ? domain.getPerformedBy().getUsername() : null
        );
    }
}
