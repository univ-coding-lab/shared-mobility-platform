package com.next.rentalservice.saga.command;

import com.next.rentalservice.saga.context.SagaContext;

public interface SagaCommand {

    SagaCommandResult execute(SagaContext context);

    SagaCommandResult compensate(SagaContext context);

    String getName();

    default boolean validate(SagaContext context) {
        return context != null && context.getSagaId() != null;
    }

    default int getOrder() {
        return 0;
    }

    default boolean supportsAsync() {
        return false;
    }
}
