package application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class ProductJpaEntity {

    @Id
    @Column(name = "identifier", nullable = false, length = 64)
    private String identifier;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "product_type", nullable = false, length = 30)
    private String productType;

    @Column(name = "status", nullable = false, length = 40)
    private String status;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_identification", nullable = false)
    private SellerJpaEntity seller;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private CategoryJpaEntity category;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "product_variants",
            joinColumns = @JoinColumn(name = "product_identifier"))
    private List<ProductVariantEmbeddable> variants = new ArrayList<>();
}
