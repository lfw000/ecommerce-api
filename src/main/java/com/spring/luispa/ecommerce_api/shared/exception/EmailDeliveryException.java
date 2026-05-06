package com.spring.luispa.ecommerce_api.shared.exception;

public class EmailDeliveryException extends InfrastructureException {

    private final String recipient;
    private final String template;

    public EmailDeliveryException(String recipient, String template, Throwable cause) {
        super(
                String.format("Failed to send email to %s using template %s",
                        recipient, template),
                "EMAIL_DELIVERY_ERROR",
                500,
                cause);
        this.recipient = recipient;
        this.template = template;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getTemplate() {
        return template;
    }
}
