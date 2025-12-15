package com.next.rentalservice.saga.command;

import com.next.rentalservice.saga.context.SagaContext;

/**
 * Command interface for Saga steps following the Command pattern.
 * Each saga step is encapsulated as a command with execute and compensate operations.
 */
public interface SagaCommand {

    /**
     * Execute the forward action of this saga step.
     * @param context The saga execution context
     * @return Result of the command execution
     */
    SagaCommandResult execute(SagaContext context);

    /**
     * Compensate (rollback) this saga step.
     * @param context The saga execution context
     * @return Result of the compensation
     */
    SagaCommandResult compensate(SagaContext context);

    /**
     * Get the unique identifier for this command.
     * @return Command name/identifier
     */
    String getName();

    /**
     * Validate if this command can be executed in the current context.
     * @param context The saga execution context
     * @return true if validation passes, false otherwise
     */
    default boolean validate(SagaContext context) {
        return context != null && context.getSagaId() != null;
    }

    /**
     * Get the order/priority of this command in the saga sequence.
     * Lower values execute first.
     * @return Order value
     */
    default int getOrder() {
        return 0;
    }

    /**
     * Check if this command supports asynchronous execution.
     * @return true if async execution is supported
     */
    default boolean supportsAsync() {
        return false;
    }
}
