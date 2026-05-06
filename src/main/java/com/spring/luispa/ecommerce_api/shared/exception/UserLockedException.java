package com.spring.luispa.ecommerce_api.shared.exception;

public class UserLockedException extends DomainException {

    private final String email;

    public UserLockedException(String email) {
        super(
                String.format("User account is locked: %s", email),
                "USER_LOCKED",
                403);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
