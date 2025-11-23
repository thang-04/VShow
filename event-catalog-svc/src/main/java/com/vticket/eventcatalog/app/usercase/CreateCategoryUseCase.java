package com.vticket.eventcatalog.app.usercase;

import com.vticket.eventcatalog.app.dto.req.CreateCategoryRequest;
import com.vticket.eventcatalog.app.dto.res.CategoryResponse;
import com.vticket.eventcatalog.app.mapper.CategoryDtoMapper;
import com.vticket.eventcatalog.domain.entity.Category;
import com.vticket.eventcatalog.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryDtoMapper categoryDtoMapper;

    public CategoryResponse execute(CreateCategoryRequest request) {
        Category category = Category.builder()
                .categoryName(request.getCategoryName())
                .categoryDescription(request.getCategoryDescription())
                .build();
        Category saved = categoryRepository.save(category);
        return categoryDtoMapper.toResponse(saved);
    }
}
