package com.next.common.domain.exception;

/**
 * Exception thrown when a service is temporarily unavailable
 */
public class ServiceUnavailableException extends BusinessException {

    public ServiceUnavailableException(String serviceName) {
        super("SERVICE_UNAVAILABLE", String.format("Service '%s' is currently unavailable", serviceName));
    }

    public ServiceUnavailableException(String serviceName, Throwable cause) {
        super("SERVICE_UNAVAILABLE",
              String.format("Service '%s' is currently unavailable", serviceName),
              cause);
    }
}
