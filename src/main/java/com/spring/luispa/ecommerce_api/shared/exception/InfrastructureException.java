package com.spring.luispa.ecommerce_api.shared.exception;

public class InfrastructureException extends BaseException {

    protected InfrastructureException(String message, String errorCode, int statusCode) {
        super(message, errorCode, statusCode);
    }

    protected InfrastructureException(String message, String errorCode, int statusCode, Throwable cause) {
        super(message, errorCode, statusCode, cause);
    }
}
