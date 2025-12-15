package com.next.rentalservice.saga.exception;

/**
 * Exception for Saga command execution failures
 */
public class SagaCommandException extends SagaException {

    private final String commandName;

    public SagaCommandException(String commandName, String message) {
        super(message);
        this.commandName = commandName;
    }

    public SagaCommandException(String commandName, String message, Throwable cause) {
        super(message, cause);
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }
}
