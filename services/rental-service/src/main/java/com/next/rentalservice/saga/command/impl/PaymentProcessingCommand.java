package com.next.rentalservice.saga.command.impl;

import com.next.common.event.saga.SagaState;
import com.next.rentalservice.saga.command.AbstractSagaCommand;
import com.next.rentalservice.saga.command.SagaCommandResult;
import com.next.rentalservice.saga.context.SagaContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@Order(2)
public class PaymentProcessingCommand extends AbstractSagaCommand {

    private static final String COMMAND_NAME = "PAYMENT_PROCESSING";

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public boolean validate(SagaContext context) {
        return super.validate(context) && context.getUserId() != null;
    }

    @Override
    protected SagaCommandResult doExecute(SagaContext context) {
        log.info("Processing payment: userId={}, rentalId={}",
                context.getUserId(), context.getRentalId());

        String transactionId = "TXN-" + context.getSagaId();
        context.putStepResult("transactionId", transactionId);

        return SagaCommandResult.success(COMMAND_NAME, SagaState.PAYMENT_COMPLETED,
                Map.of("transactionId", transactionId));
    }

    @Override
    protected SagaCommandResult doCompensate(SagaContext context) {
        log.info("Refunding payment: userId={}", context.getUserId());

        String transactionId = context.getStepResult("transactionId", String.class);
        log.info("Refunding transaction: transactionId={}", transactionId);

        return SagaCommandResult.success(COMMAND_NAME, SagaState.PAYMENT_REFUNDED);
    }
}
