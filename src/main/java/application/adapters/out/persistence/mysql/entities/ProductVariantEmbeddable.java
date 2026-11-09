package application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantEmbeddable {

    @Column(name = "attribute_name", nullable = false, length = 100)
    private String attributeName;

    @Column(name = "attribute_value", nullable = false, length = 150)
    private String value;
}
