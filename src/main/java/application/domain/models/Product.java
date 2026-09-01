package application.domain.models;

import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.ProductType;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Catalog item offered by a seller (spec Domain 5). Cannot be instantiated directly;
 * use {@link PhysicalProduct} or {@link DigitalProduct}.
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class Product implements AuditableEntity {

    private String identifier;
    private String name;
    private String description;
    private ProductStatus status;
    private Money price;
    private Seller seller;
    private Category category;
    private List<ProductVariant> variants = new ArrayList<>();

    /** @return the fulfillment nature of this product. */
    public abstract ProductType getType();

    @Override
    public String auditableType() {
        return "Product";
    }

    @Override
    public String auditableId() {
        return identifier;
    }
}
