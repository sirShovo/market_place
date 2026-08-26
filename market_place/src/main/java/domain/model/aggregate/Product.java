package domain.model.aggregate;
import domain.model.valueobject.*;

/**
 * Domain aggregate root representing a Product in the catalog.
 */
public class Product {
    private Long id;
    private String name;
    private ProductType type;
    private ProductStatus status;
    private Money price;
    private Long sellerId;
    
    /**
     * Constructor for Product.
     * @param name The product name.
     * @param type The product type (PHYSICAL or DIGITAL).
     * @param price The product price.
     * @param sellerId The ID of the seller who owns the product.
     */
    public Product(String name, ProductType type, Money price, Long sellerId) {
        this.name = name; this.type = type; this.price = price; this.sellerId = sellerId; this.status = ProductStatus.PUBLISHED;
    }
    
    /** @return The Product ID */
    public Long getId() { return id; }
    /** @param id The Product ID to set */
    public void setId(Long id) { this.id = id; }
    
    /** @return The product type */
    public ProductType getType() { return type; }
}
