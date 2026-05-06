package com.spring.luispa.ecommerce_api.shared.exception;

public class DatabaseException extends InfrastructureException {

    public DatabaseException(String message, Throwable cause) {
        super(message, "DATABASE_ERROR", 500, cause);
    }

    public DatabaseException(String message) {
        super(message, "DATABASE_ERROR", 500);
    }
}
