package com.next.rentalservice.saga.command.impl;

import com.next.common.event.saga.SagaState;
import com.next.rentalservice.saga.command.AbstractSagaCommand;
import com.next.rentalservice.saga.command.SagaCommandResult;
import com.next.rentalservice.saga.context.SagaContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Command for reserving a vehicle in the rental saga.
 */
@Slf4j
@Component
@Order(1)
public class VehicleReservationCommand extends AbstractSagaCommand {

    private static final String COMMAND_NAME = "VEHICLE_RESERVATION";

    // In real implementation, inject VehicleServiceClient here
    // private final VehicleServiceClient vehicleServiceClient;

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public boolean validate(SagaContext context) {
        return super.validate(context)
                && context.getVehicleId() != null
                && context.getUserId() != null;
    }

    @Override
    protected SagaCommandResult doExecute(SagaContext context) {
        log.info("Reserving vehicle: vehicleId={}, userId={}",
                context.getVehicleId(), context.getUserId());

        // TODO: In real implementation, call Vehicle Service API
        // VehicleReservationResponse response = vehicleServiceClient.reserveVehicle(
        //     context.getVehicleId(), context.getUserId());

        // Store reservation ID for potential compensation
        String reservationId = "RES-" + context.getSagaId();
        context.putStepResult("reservationId", reservationId);

        return SagaCommandResult.success(COMMAND_NAME, SagaState.VEHICLE_RESERVED);
    }

    @Override
    protected SagaCommandResult doCompensate(SagaContext context) {
        log.info("Releasing vehicle reservation: vehicleId={}", context.getVehicleId());

        String reservationId = context.getStepResult("reservationId", String.class);
        log.info("Releasing reservation: reservationId={}", reservationId);

        // TODO: In real implementation, call Vehicle Service API
        // vehicleServiceClient.releaseReservation(reservationId);

        return SagaCommandResult.success(COMMAND_NAME, SagaState.VEHICLE_RELEASED);
    }
}
