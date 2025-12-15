package com.next.common.domain.exception;

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
