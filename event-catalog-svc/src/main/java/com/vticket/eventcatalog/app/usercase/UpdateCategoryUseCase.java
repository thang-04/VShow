package com.vticket.eventcatalog.app.usercase;

import com.vticket.commonlibs.exception.AppException;
import com.vticket.commonlibs.exception.ErrorCode;
import com.vticket.eventcatalog.app.dto.req.UpdateCategoryRequest;
import com.vticket.eventcatalog.app.dto.res.CategoryResponse;
import com.vticket.eventcatalog.app.mapper.CategoryDtoMapper;
import com.vticket.eventcatalog.domain.entity.Category;
import com.vticket.eventcatalog.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateCategoryUseCase {
    private final CategoryRepository categoryRepository;
    private final CategoryDtoMapper categoryDtoMapper;

    public CategoryResponse execute(long cateId, UpdateCategoryRequest updateCategoryRequest) {
        Category category = categoryRepository.findById(cateId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        if (updateCategoryRequest.getCategoryName() != null) category.setCategoryName(updateCategoryRequest.getCategoryName());
        if (updateCategoryRequest.getCategoryDescription() != null)
            category.setCategoryDescription(updateCategoryRequest.getCategoryDescription());
        Category updated = categoryRepository.save(category);
        return categoryDtoMapper.toResponse(updated);
    }
}
