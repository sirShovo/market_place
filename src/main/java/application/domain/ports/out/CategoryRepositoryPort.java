package application.domain.ports.out;

import application.domain.models.Category;
import java.util.List;
import java.util.Optional;

/** Persistence contract for {@link Category}. */
public interface CategoryRepositoryPort {

    Category save(Category category);

    Optional<Category> findById(Category category);

    List<Category> findAll();
}
