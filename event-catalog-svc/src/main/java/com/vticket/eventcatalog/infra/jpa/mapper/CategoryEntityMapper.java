package com.vticket.eventcatalog.infra.jpa.mapper;

import com.vticket.eventcatalog.domain.entity.Category;
import com.vticket.eventcatalog.infra.jpa.CategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryEntityMapper {

    CategoryEntity toEntity(Category category);

    Category toDomain(CategoryEntity category);

    List<Category> toDomainList(List<CategoryEntity> category);
}
