package com.vticket.eventcatalog.domain.repository;

import com.vticket.eventcatalog.domain.entity.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Category save(Category event);
    Optional<Category> findById(Long id);
    List<Category> findAll();
    void deleteById(Long id);
}
