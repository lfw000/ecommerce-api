package com.spring.luispa.ecommerce_api.shared.exception;

public class AuthorizationException extends DomainException {

    public AuthorizationException(String message) {
        super(message, "UNAUTHORIZED_ACCESS", 403);
    }

    public AuthorizationException(Long userId, String resourceType, Long resourceId) {
        super(
                String.format("User %d does not have access to %s with id: %d",
                        userId, resourceType, resourceId),
                "UNAUTHORIZED_ACCESS",
                403);
    }
}
