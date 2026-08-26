package domain.model.entity;
public class Warehouse {
    private Long id;
    private String name;
    private Long sellerId;
    public Warehouse(String name, Long sellerId) { this.name = name; this.sellerId = sellerId; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
}
