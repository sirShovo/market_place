package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.CategoryJpaEntity;
import application.domain.models.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryJpaEntity toEntity(Category domain) {
        if (domain == null) {
            return null;
        }
        CategoryJpaEntity entity = new CategoryJpaEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        return entity;
    }

    public Category toDomain(CategoryJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Category domain = new Category();
        domain.setId(entity.getId());
        domain.setName(entity.getName());
        return domain;
    }
}
