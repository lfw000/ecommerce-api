package com.spring.luispa.ecommerce_api.domain.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;

@Converter(autoApply = true)
public class ProductAttributesConverter implements AttributeConverter<ProductAttributes, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ProductAttributes attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "{}";
        }
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting attributes to JSON", e);
        }
    }

    @Override
    public ProductAttributes convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new ProductAttributes();
        }
        try {
            Map<String, String> map = mapper.readValue(dbData,
                    new TypeReference<Map<String, String>>() {});

            return new ProductAttributes(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON to attributes", e);
        }
    }
}
