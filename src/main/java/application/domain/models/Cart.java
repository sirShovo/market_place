package application.domain.models;

import application.domain.valueobjects.CartStatus;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Provisional product selection before an order is committed (spec Domain 7, state 1).
 */
@Getter
@Setter
@NoArgsConstructor
public class Cart implements AuditableEntity {

    private Long id;
    private Buyer buyer;
    private List<CartItem> items = new ArrayList<>();
    private CartStatus status;

    @Override
    public String auditableType() {
        return "Cart";
    }

    @Override
    public String auditableId() {
        return id != null ? id.toString() : null;
    }
}
