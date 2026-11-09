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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_movements")
@Getter
@Setter
@NoArgsConstructor
public class InventoryMovementJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItemJpaEntity inventoryItem;

    @Column(name = "movement_type", nullable = false, length = 40)
    private String type;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "occurred_on", nullable = false)
    private LocalDateTime occurredOn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "performed_by_user_id")
    private UserJpaEntity performedBy;
}
