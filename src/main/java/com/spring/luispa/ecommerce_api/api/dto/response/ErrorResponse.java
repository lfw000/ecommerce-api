package com.spring.luispa.ecommerce_api.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API error response")
public class ErrorResponse {

    @Schema(description = "Error timestamp",
            example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code",
            example = "400")
    private int status;

    @Schema(description = "Short error description",
            example = "Bad Request")
    private String error;

    @Schema(description = "Detailed error message",
            example = "Email already registered")
    private String message;

    @Schema(description = "URL that generated the error",
            example = "/api/auth/register")
    private String path;

    @Schema(description = "Field by field validation errors (if applicable)")
    private List<ValidationError> validationErrors;

    @Schema(description = "Validation errors in map format (alternative)")
    private Map<String, String> errors;

    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public ErrorResponse(int status, String error, String message, String path,
                         List<ValidationError> validationErrors) {
        this(status, error, message, path);
        this.validationErrors = validationErrors;
    }

    public ErrorResponse(int status, String error, String message, String path,
                         Map<String, String> errors) {
        this(status, error, message, path);
        this.errors = errors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<ValidationError> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}
