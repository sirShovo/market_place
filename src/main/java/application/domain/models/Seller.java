package application.domain.models;

import application.domain.valueobjects.SellerStatus;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Participant that registers and maintains products (spec Domain 3). Sellers cannot
 * self-register: they are onboarded by an {@code ADMIN} together with their first
 * warehouse (spec flow 6.1.1).
 */
@Getter
@Setter
@NoArgsConstructor
public class Seller extends Person implements AuditableEntity {

    private SellerStatus status;
    private User onboardedBy;
    private List<Warehouse> warehouses = new ArrayList<>();

    @Override
    public String auditableType() {
        return "Seller";
    }

    @Override
    public String auditableId() {
        return getIdentification() != null ? getIdentification().value() : null;
    }
}
