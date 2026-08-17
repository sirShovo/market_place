package com.marketplace.domain.model.aggregate;

import com.marketplace.domain.exception.DomainValidationException;
import com.marketplace.domain.model.valueobject.Money;
import com.marketplace.domain.model.valueobject.ProductStatus;
import com.marketplace.domain.model.valueobject.StockQuantity;
import java.time.LocalDateTime;

/**
 * Domain aggregate root representing a Product in the catalog.
 * 
 * Responsible for enforcing business rules regarding its own state, 
 * including safe stock manipulation, status transitions based on stock levels, 
 * and details updates.
 */
public class Product {
    private Long id;
    private String sku;
    private String title;
    private String description;
    private Money price;
    private StockQuantity stock;
    private ProductStatus status;
    private Long sellerId;
    private Long categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Default constructor for JPA and Mappers. Should not be used directly for domain logic.
     */
    protected Product() {}

    private Product(String sku, String title, String description, Money price, StockQuantity initialStock, Long sellerId, Long categoryId) {
        if (sku == null || sku.isBlank()) throw new DomainValidationException("SKU is required");
        if (title == null || title.isBlank()) throw new DomainValidationException("Title is required");
        if (sellerId == null || categoryId == null) throw new DomainValidationException("Seller and Category are required");
        
        this.sku = sku;
        this.title = title;
        this.description = description;
        this.price = price;
        this.stock = initialStock;
        this.sellerId = sellerId;
        this.categoryId = categoryId;
        this.status = initialStock.getValue() > 0 ? ProductStatus.ACTIVE : ProductStatus.OUT_OF_STOCK;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * Factory method to create a new Product.
     *
     * @param sku          Stock Keeping Unit, unique identifier for the product model.
     * @param title        The product's title.
     * @param description  Detailed description of the product.
     * @param price        The selling price.
     * @param initialStock The initial available stock.
     * @param sellerId     The ID of the user (SELLER) who owns the product.
     * @param categoryId   The ID of the category this product belongs to.
     * @return A new Product aggregate root instance.
     */
    public static Product create(String sku, String title, String description, Money price, StockQuantity initialStock, Long sellerId, Long categoryId) {
        return new Product(sku, title, description, price, initialStock, sellerId, categoryId);
    }

    /**
     * Updates the main details of the product.
     *
     * @param title       The new title.
     * @param description The new description.
     * @param price       The new price.
     * @param categoryId  The new category ID.
     */
    public void updateDetails(String title, String description, Money price, Long categoryId) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Decreases the available stock of the product by a given amount.
     * Automatically changes the status to OUT_OF_STOCK if the stock reaches zero.
     *
     * @param quantity The amount to deduct from the stock.
     */
    public void decreaseStock(int quantity) {
        this.stock = this.stock.subtract(quantity);
        if (this.stock.getValue() == 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Increases the available stock of the product by a given amount.
     * Automatically changes the status to ACTIVE if the product was out of stock.
     *
     * @param quantity The amount to add to the stock.
     */
    public void increaseStock(int quantity) {
        this.stock = this.stock.add(quantity);
        if (this.status == ProductStatus.OUT_OF_STOCK && this.stock.getValue() > 0) {
            this.status = ProductStatus.ACTIVE;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Manually deactivates the product, making it unavailable for purchase.
     */
    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSku() { return sku; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Money getPrice() { return price; }
    public StockQuantity getStock() { return stock; }
    public ProductStatus getStatus() { return status; }
    public Long getSellerId() { return sellerId; }
    public Long getCategoryId() { return categoryId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
