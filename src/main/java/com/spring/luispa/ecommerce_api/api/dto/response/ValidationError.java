package com.spring.luispa.ecommerce_api.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Validation error details by field")
public class ValidationError {

    @Schema(description = "Name of the field that failed",
            example = "email")
    private String field;

    @Schema(description = "Error message",
            example = "Email already registered")
    private String message;

    @Schema(description = "Rejected value",
            example = "usuario@example.com")
    private Object rejectedValue;

    public ValidationError(String field, String message, Object rejectedValue) {
        this.field = field;
        this.message = message;
        this.rejectedValue = rejectedValue;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    public void setRejectedValue(Object rejectedValue) {
        this.rejectedValue = rejectedValue;
    }
}
