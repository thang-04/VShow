package com.vticket.eventcatalog.infra.jpa.repository.impl;

import com.vticket.eventcatalog.domain.entity.Category;
import com.vticket.eventcatalog.domain.repository.CategoryRepository;
import com.vticket.eventcatalog.infra.jpa.CategoryEntity;
import com.vticket.eventcatalog.infra.jpa.mapper.CategoryEntityMapper;
import com.vticket.eventcatalog.infra.jpa.repository.CategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;
    private final CategoryEntityMapper categoryEntityMapper;

    @Override
    public Category save(Category category) {
       CategoryEntity saved = categoryJpaRepository.save(categoryEntityMapper.toEntity(category));
        return categoryEntityMapper.toDomain(saved);
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryJpaRepository.findById(id).map(categoryEntityMapper::toDomain);
    }

    @Override
    public List<Category> findAll() {
        return categoryEntityMapper.toDomainList(categoryJpaRepository.findAll());
    }

    @Override
    public void deleteById(Long id) {
        categoryJpaRepository.deleteById(id);
    }
}
