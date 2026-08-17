package com.marketplace.domain.model.entity;

import com.marketplace.domain.exception.DomainValidationException;
import java.time.LocalDateTime;

/**
 * Domain entity representing a product Review made by a buyer.
 * 
 * Ensures that ratings are within the valid 1-5 range and that the review
 * is properly associated with both a product and a buyer.
 */
public class Review {
    private Long id;
    private Long productId;
    private Long buyerId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    /**
     * Default constructor for JPA and Mappers. Should not be used directly for domain logic.
     */
    protected Review() {}

    private Review(Long productId, Long buyerId, int rating, String comment) {
        if (productId == null || buyerId == null) {
            throw new DomainValidationException("Product and Buyer are required for a review");
        }
        if (rating < 1 || rating > 5) {
            throw new DomainValidationException("Rating must be between 1 and 5");
        }
        this.productId = productId;
        this.buyerId = buyerId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Factory method to create a new Review.
     *
     * @param productId The ID of the product being reviewed.
     * @param buyerId   The ID of the buyer making the review.
     * @param rating    The score given (1 to 5).
     * @param comment   Optional text comment.
     * @return A new Review entity instance.
     */
    public static Review create(Long productId, Long buyerId, int rating, String comment) {
        return new Review(productId, buyerId, rating, comment);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getProductId() { return productId; }
    public Long getBuyerId() { return buyerId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
