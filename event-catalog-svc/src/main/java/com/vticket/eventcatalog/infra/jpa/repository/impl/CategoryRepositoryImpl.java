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
        try {
            log.info("{}|Saving category", prefix);
            CategoryEntity saved = categoryJpaRepository.save(categoryEntityMapper.toEntity(category));
            Category result = categoryEntityMapper.toDomain(saved);
            log.info("{}|Category saved successfully|category_id={}", prefix, result.getId());
            return result;
        } catch (Exception e) {
            log.error("{}|FAILED|Error saving category: {}", prefix, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Optional<Category> findById(Long id) {
        String prefix = "[findById]|category_id=" + id;
        try {
            Optional<Category> category = categoryJpaRepository.findById(id).map(categoryEntityMapper::toDomain);
            if (category.isEmpty()) {
                log.error("{}|FAILED|No data found", prefix);
            } else {
                log.info("{}|SUCCESS|Category found", prefix);
            }
            return category;
        } catch (Exception e) {
            log.error("{}|FAILED|Error finding category: {}", prefix, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<Category> findAll() {
        String prefix = "[findAll]";
        try {
            List<Category> categories = categoryEntityMapper.toDomainList(categoryJpaRepository.findAll());
            log.info("{}|Found {} categories", prefix, categories.size());
            return categories;
        } catch (Exception e) {
            log.error("{}|FAILED|Error finding all categories: {}", prefix, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void deleteById(Long id) {
        String prefix = "[deleteById]|category_id=" + id;
        try {
            log.info("{}|Deleting category", prefix);
            categoryJpaRepository.deleteById(id);
            log.info("{}|Category deleted successfully", prefix);
        } catch (Exception e) {
            log.error("{}|FAILED|Error deleting category: {}", prefix, e.getMessage(), e);
        }
    }
}
