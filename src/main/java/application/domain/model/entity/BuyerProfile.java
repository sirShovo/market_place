package application.domain.model.entity;

/**
 * Domain entity representing a Buyer's extended profile.
 */
public class BuyerProfile {
    private Long id;
    private Long userId;
    private String mainAddress;
    
    /**
     * Constructor for BuyerProfile.
     * @param userId The associated user's ID.
     * @param mainAddress The buyer's main shipping address.
     */
    public BuyerProfile(Long userId, String mainAddress) { this.userId = userId; this.mainAddress = mainAddress; }
    
    /** @return The BuyerProfile ID */
    public Long getId() { return id; }
    /** @param id The BuyerProfile ID to set */
    public void setId(Long id) { this.id = id; }
    
    /** @return The associated User ID */
    public Long getUserId() { return userId; }
    
    /** @return The main shipping address */
    public String getMainAddress() { return mainAddress; }
}
