package com.spring.luispa.ecommerce_api.shared.exception;

public class InvalidStateException extends DomainException {

    public InvalidStateException(String resourceType, Long resourceId, String currentState, String requiredState) {
        super(
                String.format("Cannot perform operation. %s %d is in state: %s. Required: %s",
                        resourceType, resourceId, currentState, requiredState),
                "INVALID_STATE",
                400);
    }

    public InvalidStateException(String message) {
        super(message, "INVALID_STATE", 400);
    }
}
