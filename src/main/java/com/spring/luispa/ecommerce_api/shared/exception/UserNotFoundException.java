package com.spring.luispa.ecommerce_api.shared.exception;

public class UserNotFoundException extends DomainException {

    private final String email;

    public UserNotFoundException(String email) {
        super(
                String.format("User not found: %s", email),
                "USER_NOT_FOUND",
                401);
        this.email = email;
    }

    public UserNotFoundException() {
        super("User not found", "USER_NOT_FOUND", 401);
        this.email = null;
    }

    public String getEmail() {
        return email;
    }
}
