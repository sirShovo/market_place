package application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "inventory_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_identifier", "warehouse_identifier"}))
@Getter
@Setter
@NoArgsConstructor
public class InventoryItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_identifier", nullable = false)
    private ProductJpaEntity product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_identifier", nullable = false)
    private WarehouseJpaEntity warehouse;

    @Column(name = "stock", nullable = false)
    private int stock;

    @Column(name = "item_condition", nullable = false, length = 40)
    private String itemCondition;
}
