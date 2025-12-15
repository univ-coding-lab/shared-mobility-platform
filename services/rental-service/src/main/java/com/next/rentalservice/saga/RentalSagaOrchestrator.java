package com.next.rentalservice.saga;

import com.next.common.event.saga.SagaState;
import com.next.rentalservice.saga.command.SagaCommand;
import com.next.rentalservice.saga.command.SagaCommandResult;
import com.next.rentalservice.saga.context.SagaContext;
import com.next.rentalservice.saga.exception.SagaException;
import com.next.rentalservice.saga.model.SagaStepRecord;
import com.next.rentalservice.saga.registry.SagaCommandRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalSagaOrchestrator {

    private final RentalSagaRepository sagaRepository;
    private final SagaCommandRegistry commandRegistry;

    public RentalSaga executeSaga(String rentalId, String vehicleId, String userId) {
        String sagaId = UUID.randomUUID().toString();

        SagaContext context = SagaContext.builder()
                .sagaId(sagaId)
                .rentalId(rentalId)
                .vehicleId(vehicleId)
                .userId(userId)
                .build();

        RentalSaga saga = RentalSaga.builder()
                .sagaId(sagaId)
                .rentalId(rentalId)
                .vehicleId(vehicleId)
                .userId(userId)
                .currentState(SagaState.STARTED)
                .startedAt(LocalDateTime.now())
                .retryCount(0)
                .build();

        saga = sagaRepository.save(saga);
        log.info("Saga started: sagaId={}, rentalId={}", sagaId, rentalId);

        List<SagaCommand> executedCommands = new ArrayList<>();

        for (SagaCommand command : commandRegistry.getOrderedCommands()) {
            try {
                SagaCommandResult result = command.execute(context);

                saga.addStep(toStepRecord(result));
                saga.setCurrentState(result.getResultState());
                saga = sagaRepository.save(saga);

                if (!result.isSuccess()) {
                    log.warn("Command failed: {}, sagaId={}", command.getName(), sagaId);
                    return compensate(saga, context, executedCommands, result.getErrorMessage());
                }

                executedCommands.add(command);

            } catch (Exception e) {
                log.error("Command execution error: {}, sagaId={}", command.getName(), sagaId, e);
                return compensate(saga, context, executedCommands, e.getMessage());
            }
        }

        saga.setCurrentState(SagaState.COMPLETED);
        saga.setCompletedAt(LocalDateTime.now());
        saga.addStep(SagaStepRecord.builder()
                .stepName("SAGA_COMPLETION")
                .state(SagaState.COMPLETED)
                .executedAt(LocalDateTime.now())
                .success(true)
                .build());

        saga = sagaRepository.save(saga);
        log.info("Saga completed: sagaId={}, duration={}ms", sagaId,
                Duration.between(saga.getStartedAt(), saga.getCompletedAt()).toMillis());

        return saga;
    }

    public CompletableFuture<RentalSaga> executeSagaAsync(String rentalId, String vehicleId, String userId) {
        return CompletableFuture.supplyAsync(() -> executeSaga(rentalId, vehicleId, userId));
    }

    private RentalSaga compensate(RentalSaga saga, SagaContext context,
                                  List<SagaCommand> executedCommands, String reason) {

        log.warn("Starting saga compensation: sagaId={}, reason={}", saga.getSagaId(), reason);

        saga.setCurrentState(SagaState.COMPENSATING);
        saga.setFailureReason(reason);
        saga = sagaRepository.save(saga);

        Collections.reverse(executedCommands);

        for (SagaCommand command : executedCommands) {
            try {
                SagaCommandResult result = command.compensate(context);
                saga.addStep(toCompensationStepRecord(command.getName(), result));
                saga = sagaRepository.save(saga);

                if (!result.isSuccess()) {
                    log.error("Compensation failed: command={}, sagaId={}",
                            command.getName(), saga.getSagaId());

                }
            } catch (Exception e) {
                log.error("Compensation error: command={}, sagaId={}",
                        command.getName(), saga.getSagaId(), e);

            }
        }

        saga.setCurrentState(SagaState.FAILED);
        saga.setCompletedAt(LocalDateTime.now());
        saga = sagaRepository.save(saga);

        log.info("Saga compensation completed: sagaId={}", saga.getSagaId());
        return saga;
    }

    public RentalSaga retrySaga(String sagaId) {
        RentalSaga saga = sagaRepository.findById(sagaId)
                .orElseThrow(() -> new SagaException("Saga not found: " + sagaId));

        if (!saga.hasFailed()) {
            throw new SagaException("Cannot retry non-failed saga: " + sagaId);
        }

        int newRetryCount = saga.getRetryCount() + 1;

        log.info("Retrying saga: sagaId={}, retryCount={}", sagaId, newRetryCount);

        RentalSaga retried = executeSaga(saga.getRentalId(), saga.getVehicleId(), saga.getUserId());
        retried.setRetryCount(newRetryCount);
        return sagaRepository.save(retried);
    }

    public Optional<RentalSaga> getSaga(String sagaId) {
        return sagaRepository.findById(sagaId);
    }

    public Optional<RentalSaga> getSagaByRentalId(String rentalId) {
        return sagaRepository.findByRentalId(rentalId);
    }

    public Optional<RentalSaga> getSagaByVehicleId(String vehicleId) {
        return sagaRepository.findByVehicleId(vehicleId);
    }

    private SagaStepRecord toStepRecord(SagaCommandResult result) {
        return SagaStepRecord.builder()
                .stepName(result.getCommandName())
                .state(result.getResultState())
                .executedAt(result.getExecutedAt())
                .success(result.isSuccess())
                .errorMessage(result.getErrorMessage())
                .build();
    }

    private SagaStepRecord toCompensationStepRecord(String commandName, SagaCommandResult result) {
        return SagaStepRecord.builder()
                .stepName(commandName + "_COMPENSATION")
                .state(result.getResultState())
                .executedAt(result.getExecutedAt())
                .success(result.isSuccess())
                .errorMessage(result.getErrorMessage())
                .compensationAction("Compensated: " + commandName)
                .build();
    }
}
