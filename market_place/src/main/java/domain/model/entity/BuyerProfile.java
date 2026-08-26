package domain.model.entity;
public class BuyerProfile {
    private Long id;
    private Long userId;
    private String mainAddress;
    public BuyerProfile(Long userId, String mainAddress) { this.userId = userId; this.mainAddress = mainAddress; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public String getMainAddress() { return mainAddress; }
}
