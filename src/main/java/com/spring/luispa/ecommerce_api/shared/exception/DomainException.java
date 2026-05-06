package com.spring.luispa.ecommerce_api.shared.exception;

public class DomainException extends BaseException {

    protected DomainException(String message, String errorCode, int statusCode) {
        super(message, errorCode, statusCode);
    }

    protected DomainException(String message, String errorCode, int statusCode, Throwable cause) {
        super(message, errorCode, statusCode, cause);
    }
}
