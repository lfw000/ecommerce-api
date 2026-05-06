package com.spring.luispa.ecommerce_api.shared.exception;

public class JwtMalformedException extends JwtException {

    public JwtMalformedException(String message) {
        super(
                String.format("Malformed JWT token: %s", message),
                "JWT_MALFORMED",
                400);
    }
}
