package com.spring.luispa.ecommerce_api.shared.exception;

public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(Class<?> resourceClass, Long id) {
        super(
                String.format("%s not found with id: %d", resourceClass.getSimpleName(), id),
                "RESOURCE_NOT_FOUND",
                404);
    }

    public ResourceNotFoundException(String resourceType, Long id) {
        super(
                String.format("%s not found with id: %d", resourceType, id),
                "RESOURCE_NOT_FOUND",
                404);
    }

    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND", 404);
    }
}
