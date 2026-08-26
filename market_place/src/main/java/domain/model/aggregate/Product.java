package domain.model.aggregate;
import domain.model.valueobject.*;
public class Product {
    private Long id;
    private String name;
    private ProductType type;
    private ProductStatus status;
    private Money price;
    private Long sellerId;
    public Product(String name, ProductType type, Money price, Long sellerId) {
        this.name = name; this.type = type; this.price = price; this.sellerId = sellerId; this.status = ProductStatus.PUBLISHED;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ProductType getType() { return type; }
}
