package com.spring.luispa.ecommerce_api.shared.exception;

public class JwtException extends InfrastructureException {

    protected JwtException(String message, String errorCode, int statusCode) {
        super(message, errorCode, statusCode);
    }
}
