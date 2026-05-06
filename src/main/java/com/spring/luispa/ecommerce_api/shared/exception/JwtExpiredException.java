package com.spring.luispa.ecommerce_api.shared.exception;

import java.util.Date;

public class JwtExpiredException extends JwtException {

    private final Date expirationDate;

    public JwtExpiredException(Date expirationDate) {
        super(
                String.format("JWT token expired at: %s", expirationDate),
                "JWT_EXPIRED",
                401);
        this.expirationDate = expirationDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }
}
