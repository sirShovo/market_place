package application.adapters.in.rest.mappers;

import application.adapters.in.rest.responses.OrderItemResponse;
import application.adapters.in.rest.responses.OrderResponse;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderRestMapper {

    public Order toProbe(String identifier) {
        Order probe = new Order();
        probe.setIdentifier(identifier);
        return probe;
    }

    public OrderResponse toResponse(Order domain) {
        if (domain == null) {
            return null;
        }
        List<OrderItemResponse> items = domain.getItems() != null
                ? domain.getItems().stream().map(this::toItemResponse).toList()
                : List.of();

        return new OrderResponse(
                domain.getIdentifier(),
                domain.getBuyer() != null && domain.getBuyer().getIdentification() != null
                        ? domain.getBuyer().getIdentification().value()
                        : null,
                domain.getStatus() != null ? domain.getStatus().getCode() : null,
                domain.getTotal() != null ? domain.getTotal().amount() : null,
                domain.getCreatedAt(),
                items
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getProduct() != null ? item.getProduct().getIdentifier() : null,
                item.getProduct() != null ? item.getProduct().getName() : null,
                item.getVariant() != null ? item.getVariant().getAttributeName() : null,
                item.getVariant() != null ? item.getVariant().getValue() : null,
                item.getQuantity(),
                item.getUnitPrice() != null ? item.getUnitPrice().amount() : null,
                item.getUnitPrice() != null ? item.getSubtotal().amount() : null,
                item.getWarehouse() != null ? item.getWarehouse().getIdentifier() : null
        );
    }
}
