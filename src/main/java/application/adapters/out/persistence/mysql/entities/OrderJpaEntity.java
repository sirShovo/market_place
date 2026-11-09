package application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderJpaEntity {

    @Id
    @Column(name = "identifier", nullable = false, length = 64)
    private String identifier;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "buyer_identification", nullable = false)
    private BuyerJpaEntity buyer;

    @Column(name = "total", nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    @Column(name = "status", nullable = false, length = 40)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItemJpaEntity> items = new ArrayList<>();
}
