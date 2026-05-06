package com.spring.luispa.ecommerce_api.shared.exception;

public class JwtSignatureException extends JwtException {

    public JwtSignatureException(String message) {
        super(
                String.format("Invalid JWT signature: %s", message),
                "JWT_INVALID_SIGNATURE",
                401);
    }
}
