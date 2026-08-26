package domain.model.entity;
public class Category {
    private Long id;
    private String name;
    public Category(String name) { this.name = name; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
}
