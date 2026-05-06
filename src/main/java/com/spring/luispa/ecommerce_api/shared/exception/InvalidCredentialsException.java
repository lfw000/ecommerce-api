package com.spring.luispa.ecommerce_api.shared.exception;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Invalid email or password", "INVALID_CREDENTIALS", 401);
    }

    public InvalidCredentialsException(String email) {
        super(String.format("Invalid credentials for email: %s", email),
                "INVALID_CREDENTIALS",
                401);
    }
}
