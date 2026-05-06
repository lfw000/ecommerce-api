package com.spring.luispa.ecommerce_api.shared.exception;

public class UserAccountExpiredException extends DomainException {

    private final String email;

    public UserAccountExpiredException(String email) {
        super(
                String.format("User account expired: %s", email),
                "USER_ACCOUNT_EXPIRED",
                403);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
