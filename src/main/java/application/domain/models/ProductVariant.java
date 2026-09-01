package application.domain.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A colour / size / model difference of a {@link Product} (spec Domain 5).
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductVariant {

    private String attributeName;
    private String value;
}
