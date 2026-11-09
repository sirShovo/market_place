package application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
public class WarehouseJpaEntity {

    @Id
    @Column(name = "identifier", nullable = false, length = 64)
    private String identifier;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false, length = 40)
    private String type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_identification")
    private SellerJpaEntity owner;

    @Column(name = "location")
    private String location;
}
