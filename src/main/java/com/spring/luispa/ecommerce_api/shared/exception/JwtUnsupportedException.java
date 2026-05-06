package com.spring.luispa.ecommerce_api.shared.exception;

public class JwtUnsupportedException extends JwtException {

    public JwtUnsupportedException(String message) {
        super(
                String.format("Unsupported JWT token: %s", message),
                "JWT_UNSUPPORTED",
                400);
    }
}
