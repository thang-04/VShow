package com.vticket.eventcatalog.app.mapper;

import com.vticket.eventcatalog.app.dto.res.CategoryResponse;
import com.vticket.eventcatalog.domain.entity.Category;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryDtoMapper {
    CategoryResponse toResponse(Category category);
    List<CategoryResponse> toResponseList(List<Category> categories);
}
