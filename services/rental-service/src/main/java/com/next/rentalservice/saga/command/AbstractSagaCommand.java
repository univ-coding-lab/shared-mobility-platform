package com.next.rentalservice.saga.command;

import com.next.rentalservice.saga.context.SagaContext;
import com.next.rentalservice.saga.exception.SagaCommandException;
import com.next.rentalservice.saga.exception.SagaCompensationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for saga commands providing common functionality.
 * Uses Template Method pattern for consistent logging, validation, and error handling.
 */
@Slf4j
public abstract class AbstractSagaCommand implements SagaCommand {

    @Override
    public final SagaCommandResult execute(SagaContext context) {
        log.info("Executing command: {}, sagaId={}", getName(), context.getSagaId());

        try {
            if (!validate(context)) {
                log.warn("Command validation failed: {}, sagaId={}", getName(), context.getSagaId());
                return SagaCommandResult.failure(getName(), "Validation failed");
            }

            SagaCommandResult result = doExecute(context);

            if (result.isSuccess()) {
                log.info("Command executed successfully: {}, sagaId={}", getName(), context.getSagaId());
            } else {
                log.warn("Command execution failed: {}, sagaId={}, error={}",
                        getName(), context.getSagaId(), result.getErrorMessage());
            }

            return result;

        } catch (Exception e) {
            log.error("Command execution error: {}, sagaId={}", getName(), context.getSagaId(), e);
            throw new SagaCommandException(getName(), "Execution failed: " + e.getMessage(), e);
        }
    }

    @Override
    public final SagaCommandResult compensate(SagaContext context) {
        log.info("Compensating command: {}, sagaId={}", getName(), context.getSagaId());

        try {
            SagaCommandResult result = doCompensate(context);

            if (result.isSuccess()) {
                log.info("Command compensated successfully: {}, sagaId={}", getName(), context.getSagaId());
            } else {
                log.warn("Command compensation failed: {}, sagaId={}, error={}",
                        getName(), context.getSagaId(), result.getErrorMessage());
            }

            return result;

        } catch (Exception e) {
            log.error("Command compensation error: {}, sagaId={}", getName(), context.getSagaId(), e);
            throw new SagaCompensationException(getName(), "Compensation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Template method for actual execution logic.
     * Subclasses implement this to perform the forward action.
     */
    protected abstract SagaCommandResult doExecute(SagaContext context);

    /**
     * Template method for actual compensation logic.
     * Subclasses implement this to perform the rollback action.
     */
    protected abstract SagaCommandResult doCompensate(SagaContext context);
}
