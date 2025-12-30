package com.vticket.eventcatalog.infra.jpa.repository.impl;

import com.vticket.eventcatalog.domain.entity.Category;
import com.vticket.eventcatalog.domain.repository.CategoryRepository;
import com.vticket.eventcatalog.infra.jpa.CategoryEntity;
import com.vticket.eventcatalog.infra.jpa.mapper.CategoryEntityMapper;
import com.vticket.eventcatalog.infra.jpa.repository.CategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;
    private final CategoryEntityMapper categoryEntityMapper;

    @Override
    public Category save(Category category) {
        String prefix = "[save]|category_id=" + (category.getId() != null ? category.getId() : "new");
        log.info("{}|Saving category", prefix);
        CategoryEntity saved = categoryJpaRepository.save(categoryEntityMapper.toEntity(category));
        Category result = categoryEntityMapper.toDomain(saved);
        log.info("{}|Category saved successfully|category_id={}", prefix, result.getId());
        return result;
    }

    @Override
    public Optional<Category> findById(Long id) {
        String prefix = "[findById]|category_id=" + id;
        Optional<Category> category = categoryJpaRepository.findById(id).map(categoryEntityMapper::toDomain);
        if (category.isEmpty()) {
            log.error("{}|FAILED|No data found", prefix);
        } else {
            log.info("{}|SUCCESS|Category found", prefix);
        }
        return category;
    }

    @Override
    public List<Category> findAll() {
        return categoryEntityMapper.toDomainList(categoryJpaRepository.findAll());
    }

    @Override
    public void deleteById(Long id) {
        String prefix = "[deleteById]|category_id=" + id;
        log.info("{}|Deleting category", prefix);
        categoryJpaRepository.deleteById(id);
        log.info("{}|Category deleted successfully", prefix);
    }
}
