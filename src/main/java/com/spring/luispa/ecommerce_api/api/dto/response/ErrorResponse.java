package com.spring.luispa.ecommerce_api.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.spring.luispa.ecommerce_api.shared.exception.DomainException;
import com.spring.luispa.ecommerce_api.shared.exception.InfrastructureException;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API error response")
public class ErrorResponse {

    @Schema(description = "Timestamp when the error occurred",
            example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code",
            example = "400")
    private int status;

    @Schema(description = "Short error description",
            example = "Bad Request")
    private String error;

    @Schema(description = "Application-specific error code",
        example = "DUPLICATE_RESOURCE",
        allowableValues = {
            // Domain errors
            "RESOURCE_NOT_FOUND", "DUPLICATE_RESOURCE", "UNAUTHORIZED_ACCESS",
            "BUSINESS_RULE_VIOLATION", "INVALID_STATE", "INSUFFICIENT_STOCK",
            "INVALID_CREDENTIALS", "EMPTY_CART", "PRODUCT_NOT_ACTIVE",
            "ACCESS_DENIED",
            // Infrastructure errors
            "DATABASE_ERROR", "PAYMENT_GATEWAY_ERROR", "EXTERNAL_SERVICE_ERROR",
            "EMAIL_DELIVERY_ERROR",
            // Request errors
            "VALIDATION_ERROR", "MALFORMED_JSON", "MISSING_PARAMETER",
            "TYPE_MISMATCH", "ILLEGAL_ARGUMENT",
            // Generic
            "INTERNAL_ERROR"})
    private String errorCode;

    @Schema(description = "Detailed error message",
            example = "Email already registered")
    private String message;

    @Schema(description = "URL that generated the error",
            example = "/api/auth/register")
    private String path;

    @Schema(description = "Field by field validation errors (if applicable)")
    private List<ValidationError> validationErrors;

    @Schema(description = "Validation errors in map format (alternative format)")
    private Map<String, String> errors;

    public ErrorResponse(int status, String error, String errorCode, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
    }

    public ErrorResponse(int status, String error, String errorCode, String message, String path, List<ValidationError> validationErrors) {
        this(status, error, errorCode, message, path);
        this.validationErrors = validationErrors;
    }

    public ErrorResponse(int status, String error, String errorCode, String message, String path, Map<String, String> errors) {
        this(status, error, errorCode, message, path);
        this.errors = errors;
    }

    // Getters and setters

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

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
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


    // Helper methods

    public static ErrorResponse of(DomainException ex, String path) {
        return new ErrorResponse(
                ex.getStatusCode(),
                getHttpStatusName(ex.getStatusCode()),
                ex.getErrorCode(),
                ex.getMessage(),
                path);
    }

    public static ErrorResponse of(InfrastructureException ex, String path) {
        return new ErrorResponse(
                ex.getStatusCode(),
                getHttpStatusName(ex.getStatusCode()),
                ex.getErrorCode(),
                "A technical error ocurred. Please try again later.",
                path);
    }

    private static String getHttpStatusName(int statusCode) {
        return switch (statusCode) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 409 -> "Conflict";
            case 500 -> "Internal Server Error";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            default -> "Error";
        };
    }
}
