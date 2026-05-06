package com.spring.luispa.ecommerce_api.shared.exception;

import java.util.Map;

public class ValidationException extends DomainException {

    private final Map<String, String> errors;

    public ValidationException(String message, Map<String, String> errors) {
        super(message, "VALIDATION_ERROR", 400);
        this.errors = errors;
    }

    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR", 400);
        this.errors = null;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
