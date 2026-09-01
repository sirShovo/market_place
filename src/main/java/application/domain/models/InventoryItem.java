package application.domain.models;

import application.domain.valueobjects.InventoryItemCondition;
import application.domain.valueobjects.StockQuantity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Distributed stock bound to one product and one warehouse (spec Domain 6). Stock is
 * never negative; {@code DAMAGED} stock cannot be reserved (spec §11).
 */
@Getter
@Setter
@NoArgsConstructor
public class InventoryItem implements AuditableEntity {

    private Long id;
    private Product product;
    private Warehouse warehouse;
    private StockQuantity stock;
    private InventoryItemCondition condition;

    @Override
    public String auditableType() {
        return "InventoryItem";
    }

    @Override
    public String auditableId() {
        return id != null ? id.toString() : null;
    }
}
