package com.next.rentalservice.saga.exception;

/**
 * Base exception for Saga operations
 */
public class SagaException extends RuntimeException {

    public SagaException(String message) {
        super(message);
    }

    public SagaException(String message, Throwable cause) {
        super(message, cause);
    }
}
