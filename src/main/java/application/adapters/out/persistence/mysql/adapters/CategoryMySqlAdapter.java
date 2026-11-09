package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.CategoryJpaEntity;
import application.adapters.out.persistence.mysql.mappers.CategoryMapper;
import application.adapters.out.persistence.mysql.repositories.CategoryJpaRepository;
import application.domain.models.Category;
import application.domain.ports.out.CategoryRepositoryPort;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryMySqlAdapter implements CategoryRepositoryPort {

    private final CategoryJpaRepository categoryJpaRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public Category save(Category category) {
        CategoryJpaEntity saved = categoryJpaRepository.save(categoryMapper.toEntity(category));
        return categoryMapper.toDomain(saved);
    }

    @Override
    public Optional<Category> findById(Category category) {
        if (category == null || category.getId() == null) {
            return Optional.empty();
        }
        return categoryJpaRepository.findById(category.getId())
                .map(categoryMapper::toDomain);
    }

    @Override
    public List<Category> findAll() {
        return categoryJpaRepository.findAll().stream()
                .map(categoryMapper::toDomain)
                .toList();
    }
}
