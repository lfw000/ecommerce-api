package com.spring.luispa.ecommerce_api.mappers;

import com.spring.luispa.ecommerce_api.api.dto.response.CategoryResponse;
import com.spring.luispa.ecommerce_api.domain.product.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(source = "parentCategory.id", target = "parentCategoryId")
    @Mapping(source = "parentCategory.name", target = "parentCategoryName")
    @Mapping(target = "fullPath", expression = "java(category.getFullPath())")
    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseList(List<Category> categories);

    @Named("getFullPath")
    default String getFullPath(Category category) {
        if (category == null) {
            return null;
        }

        return category.getFullPath();
    }
}
