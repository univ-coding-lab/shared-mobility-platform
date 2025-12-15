package com.next.rentalservice.saga.exception;

public class SagaCompensationException extends SagaCommandException {

    public SagaCompensationException(String commandName, String message) {
        super(commandName, message);
    }

    public SagaCompensationException(String commandName, String message, Throwable cause) {
        super(commandName, message, cause);
    }
}
