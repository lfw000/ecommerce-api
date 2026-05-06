package com.spring.luispa.ecommerce_api.shared.exception;

public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String message, String errorCode) {
        super(message, errorCode, 400);
    }

    public BusinessRuleException(String message) {
        super(message, "BUSINESS_RULE_VIOLATION", 400);
    }
}
