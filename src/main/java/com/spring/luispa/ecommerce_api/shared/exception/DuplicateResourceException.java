package com.spring.luispa.ecommerce_api.shared.exception;

public class DuplicateResourceException extends DomainException {

    public DuplicateResourceException(Class<?> resourceClass, String field, String value) {
        super(
                String.format("%s already exists with %s: %s", resourceClass.getSimpleName(), field, value),
                "DUPLICATE_RESOURCE",
                409);
    }

    public DuplicateResourceException(String message) {
        super(message, "DUPLICATE_RESOURCE", 409);
    }
}
