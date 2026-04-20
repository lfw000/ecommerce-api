package com.spring.luispa.ecommerce_api.mappers;

import com.spring.luispa.ecommerce_api.api.dto.request.AddAddressRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.AddressResponse;
import com.spring.luispa.ecommerce_api.domain.user.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    // Entity -> Response
    AddressResponse toResponse(Address address);
    List<AddressResponse> toResponseList(List<Address> addresses);

    // Request -> Entity
    Address toEntity(AddAddressRequest request);
}
