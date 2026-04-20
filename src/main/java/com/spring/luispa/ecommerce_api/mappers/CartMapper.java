package com.spring.luispa.ecommerce_api.mappers;

import com.spring.luispa.ecommerce_api.api.dto.response.CartItemResponse;
import com.spring.luispa.ecommerce_api.api.dto.response.CartResponse;
import com.spring.luispa.ecommerce_api.domain.cart.Cart;
import com.spring.luispa.ecommerce_api.domain.cart.CartItem;
import com.spring.luispa.ecommerce_api.domain.cart.CartItemHelper;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
    imports = CartItemHelper.class)
public interface CartMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "totalItems", target = "totalItems")
    @Mapping(target = "items", source = "items", qualifiedByName = "toItemResponse")
    CartResponse toResponse(Cart cart);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "totalItems", target = "totalItems")
    @Mapping(target = "items", source = "items", qualifiedByName = "toItemResponseWithImage")
    CartResponse toResponseWithImage(Cart cart);

    // Mapping items to CartItemResponse

    @Named("toItemResponse")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.sku", target = "productSku")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "price", target = "unitPrice")
    CartItemResponse toItemResponse(CartItem item);

    @Named("toItemResponseWithImage")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.sku", target = "productSku")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "price", target = "unitPrice")
    @Mapping(target = "productImage", expression = "java(CartItemHelper.getFirstImageUrl(item))")
    CartItemResponse toItemResponseWithImage(CartItem item);
}
