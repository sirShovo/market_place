package application.domain.valueobjects;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Generic controlled business catalog. Every catalog value that needs a business
 * {@code code}, a display {@code name} and a {@code description} inherits from this
 * abstraction. Instances are immutable and compared solely by {@code code}.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class DomainCatalog {

    @EqualsAndHashCode.Include
    private final String code;
    private final String name;
    private final String description;

    protected DomainCatalog(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    @Override
    public String toString() {
        return code;
    }
}
