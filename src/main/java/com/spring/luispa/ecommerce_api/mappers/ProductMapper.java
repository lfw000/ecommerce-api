package com.spring.luispa.ecommerce_api.mappers;

import com.spring.luispa.ecommerce_api.api.dto.request.CreateProductRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.ProductResponse;
import com.spring.luispa.ecommerce_api.domain.product.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // Entity -> Response

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "attributes", target = "attributes")
    ProductResponse toResponse(Product product);

    List<ProductResponse> toResponseList(List<Product> products);

    // Request -> Entity

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "stock", defaultValue = "0")
    @Mapping(target = "attributes", ignore = true)
    Product toEntity(CreateProductRequest request);
}
