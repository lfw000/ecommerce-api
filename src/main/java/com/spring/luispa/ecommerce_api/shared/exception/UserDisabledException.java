package com.spring.luispa.ecommerce_api.shared.exception;

public class UserDisabledException extends DomainException {

    private final String email;

    public UserDisabledException(String email) {
        super(
                String.format("User account is disabled: %s", email),
                "USER_DISABLED",
                403);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
