package application.domain.models;

import application.domain.valueobjects.WarehouseType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Physical storage location (spec Domain 4). {@code owner} is present only when
 * {@code type == SELLER}.
 */
@Getter
@Setter
@NoArgsConstructor
public class Warehouse implements AuditableEntity {

    private String identifier;
    private String name;
    private WarehouseType type;
    private Seller owner;
    private String location;

    @Override
    public String auditableType() {
        return "Warehouse";
    }

    @Override
    public String auditableId() {
        return identifier;
    }
}
