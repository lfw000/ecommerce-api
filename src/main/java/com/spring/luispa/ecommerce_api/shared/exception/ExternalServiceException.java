package com.spring.luispa.ecommerce_api.shared.exception;

public class ExternalServiceException extends InfrastructureException {

    private final String serviceName;
    private final String endpoint;

    public ExternalServiceException(String serviceName, String endpoint, String message, Throwable cause) {
        super(String.format("External service %s failed at %s: %s",
                        serviceName, endpoint, message),
                "EXTERNAL_SERVICE_ERROR",
                503,
                cause);
        this.serviceName = serviceName;
        this.endpoint = endpoint;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
