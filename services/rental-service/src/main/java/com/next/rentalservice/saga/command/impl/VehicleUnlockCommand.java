package com.next.rentalservice.saga.command.impl;

import com.next.common.event.saga.SagaState;
import com.next.rentalservice.saga.command.AbstractSagaCommand;
import com.next.rentalservice.saga.command.SagaCommandResult;
import com.next.rentalservice.saga.context.SagaContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(3)
public class VehicleUnlockCommand extends AbstractSagaCommand {

    private static final String COMMAND_NAME = "VEHICLE_UNLOCK";

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public boolean validate(SagaContext context) {
        return super.validate(context) && context.getVehicleId() != null;
    }

    @Override
    protected SagaCommandResult doExecute(SagaContext context) {
        log.info("Unlocking vehicle: vehicleId={}", context.getVehicleId());

        return SagaCommandResult.success(COMMAND_NAME, SagaState.VEHICLE_UNLOCKED);
    }

    @Override
    protected SagaCommandResult doCompensate(SagaContext context) {
        log.info("Locking vehicle: vehicleId={}", context.getVehicleId());

        return SagaCommandResult.success(COMMAND_NAME, SagaState.VEHICLE_RESERVED);
    }
}
