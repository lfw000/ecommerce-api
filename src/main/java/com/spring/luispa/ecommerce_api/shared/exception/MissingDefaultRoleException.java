package com.spring.luispa.ecommerce_api.shared.exception;

public class MissingDefaultRoleException extends InfrastructureException {

    public MissingDefaultRoleException(String message) {
        super(message, "MISSING_DEFAULT_ROLE", 500);
    }
}
