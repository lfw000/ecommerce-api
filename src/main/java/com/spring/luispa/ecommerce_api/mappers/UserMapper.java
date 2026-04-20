package com.spring.luispa.ecommerce_api.mappers;

import com.spring.luispa.ecommerce_api.api.dto.request.RegisterRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.UserResponse;
import com.spring.luispa.ecommerce_api.domain.user.Role;
import com.spring.luispa.ecommerce_api.domain.user.User;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Entity -> Response

    @Named("basicUser")
    @Mapping(target = "address", ignore = true)
    UserResponse toResponse(User user);

    @Named("userWithRoles")
    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserResponse toResponseWithRoles(User user);

    @IterableMapping(qualifiedByName = "basicUser")
    List<UserResponse> toResponseList(List<User> users);

    // Request -> Entity

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    User toEntity(RegisterRequest request);

    // Helper methods

    default java.util.Set<String> mapRoles(java.util.Set<Role> roles) {
        if (roles == null) {
            return java.util.Collections.emptySet();
        }
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(java.util.stream.Collectors.toSet());
    }
}
