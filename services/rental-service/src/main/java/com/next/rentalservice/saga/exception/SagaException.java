package com.next.rentalservice.saga.exception;

public class SagaException extends RuntimeException {

    public SagaException(String message) {
        super(message);
    }

    public SagaException(String message, Throwable cause) {
        super(message, cause);
    }
}
