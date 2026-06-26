package com.spring.luispa.ecommerce_api.shared.exception;

public class RateLimitExceededException extends BusinessRuleException {

    public RateLimitExceededException() {
        super("Too many requests. Please try again later.", "RATE_LIMIT_EXCEEDED");
    }

    public RateLimitExceededException(String message) {
        super(message, "RATE_LIMIT_EXCEEDED");
    }
}
