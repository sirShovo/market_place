package application.domain.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Classification bucket for catalog products.
 */
@Getter
@Setter
@NoArgsConstructor
public class Category {

    private Long id;
    private String name;
}
