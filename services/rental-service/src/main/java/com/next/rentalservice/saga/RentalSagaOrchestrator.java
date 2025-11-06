package com.next.rentalservice.saga;

import com.next.common.event.saga.SagaState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Saga Orchestrator for Rental Flow
 * Manages distributed transaction across multiple services with compensation logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RentalSagaOrchestrator {

    private final RentalSagaRepository sagaRepository;

    /**
     * Start a new rental saga
     *
     * @param rentalId Rental ID
     * @param vehicleId Vehicle ID
     * @param userId User ID
     * @return Created saga
     */
    public RentalSaga startSaga(String rentalId, String vehicleId, String userId) {
        String sagaId = UUID.randomUUID().toString();

        RentalSaga saga = RentalSaga.builder()
                .sagaId(sagaId)
                .rentalId(rentalId)
                .vehicleId(vehicleId)
                .userId(userId)
                .currentState(SagaState.STARTED)
                .startedAt(LocalDateTime.now())
                .retryCount(0)
                .build();

        RentalSaga saved = sagaRepository.save(saga);
        log.info("Saga started: sagaId={}, rentalId={}, vehicleId={}", sagaId, rentalId, vehicleId);

        return saved;
    }

    /**
     * Execute vehicle reservation step
     *
     * @param saga Current saga
     * @return Updated saga
     */
    public RentalSaga executeVehicleReservation(RentalSaga saga) {
        log.info("Executing vehicle reservation: sagaId={}, vehicleId={}", saga.getSagaId(), saga.getVehicleId());

        try {
            // In a real implementation, this would call Vehicle Service API
            // For now, we simulate success
            saga.setCurrentState(SagaState.VEHICLE_RESERVED);
            saga.addStep(createSuccessStep("VEHICLE_RESERVATION", SagaState.VEHICLE_RESERVED));

            RentalSaga updated = sagaRepository.save(saga);
            log.info("Vehicle reserved successfully: sagaId={}", saga.getSagaId());
            return updated;

        } catch (Exception e) {
            log.error("Vehicle reservation failed: sagaId={}, error={}", saga.getSagaId(), e.getMessage());
            return handleStepFailure(saga, "VEHICLE_RESERVATION", e.getMessage());
        }
    }

    /**
     * Execute payment processing step
     *
     * @param saga Current saga
     * @return Updated saga
     */
    public RentalSaga executePaymentProcessing(RentalSaga saga) {
        log.info("Executing payment processing: sagaId={}, userId={}", saga.getSagaId(), saga.getUserId());

        try {
            // In a real implementation, this would call Payment Service API
            // For now, we simulate success
            saga.setCurrentState(SagaState.PAYMENT_COMPLETED);
            saga.addStep(createSuccessStep("PAYMENT_PROCESSING", SagaState.PAYMENT_COMPLETED));

            RentalSaga updated = sagaRepository.save(saga);
            log.info("Payment completed successfully: sagaId={}", saga.getSagaId());
            return updated;

        } catch (Exception e) {
            log.error("Payment processing failed: sagaId={}, error={}", saga.getSagaId(), e.getMessage());
            return handleStepFailure(saga, "PAYMENT_PROCESSING", e.getMessage());
        }
    }

    /**
     * Execute vehicle unlock step
     *
     * @param saga Current saga
     * @return Updated saga
     */
    public RentalSaga executeVehicleUnlock(RentalSaga saga) {
        log.info("Executing vehicle unlock: sagaId={}, vehicleId={}", saga.getSagaId(), saga.getVehicleId());

        try {
            // In a real implementation, this would call Vehicle Service API
            // For now, we simulate success
            saga.setCurrentState(SagaState.VEHICLE_UNLOCKED);
            saga.addStep(createSuccessStep("VEHICLE_UNLOCK", SagaState.VEHICLE_UNLOCKED));

            RentalSaga updated = sagaRepository.save(saga);
            log.info("Vehicle unlocked successfully: sagaId={}", saga.getSagaId());
            return updated;

        } catch (Exception e) {
            log.error("Vehicle unlock failed: sagaId={}, error={}", saga.getSagaId(), e.getMessage());
            return handleStepFailure(saga, "VEHICLE_UNLOCK", e.getMessage());
        }
    }

    /**
     * Complete the saga
     *
     * @param saga Current saga
     * @return Updated saga
     */
    public RentalSaga completeSaga(RentalSaga saga) {
        saga.setCurrentState(SagaState.COMPLETED);
        saga.setCompletedAt(LocalDateTime.now());
        saga.addStep(createSuccessStep("SAGA_COMPLETION", SagaState.COMPLETED));

        RentalSaga updated = sagaRepository.save(saga);
        log.info("Saga completed successfully: sagaId={}, duration={}ms",
                saga.getSagaId(),
                java.time.Duration.between(saga.getStartedAt(), saga.getCompletedAt()).toMillis());
        return updated;
    }

    /**
     * Compensate (rollback) the saga
     *
     * @param saga Current saga
     * @param reason Failure reason
     * @return Updated saga
     */
    public RentalSaga compensateSaga(RentalSaga saga, String reason) {
        log.warn("Starting saga compensation: sagaId={}, reason={}", saga.getSagaId(), reason);

        saga.setCurrentState(SagaState.COMPENSATING);
        saga.setFailureReason(reason);

        // Compensate in reverse order
        for (int i = saga.getSteps().size() - 1; i >= 0; i--) {
            RentalSaga.SagaStep step = saga.getSteps().get(i);
            if (step.isSuccess()) {
                compensateStep(saga, step);
            }
        }

        saga.setCurrentState(SagaState.FAILED);
        saga.setCompletedAt(LocalDateTime.now());

        RentalSaga updated = sagaRepository.save(saga);
        log.info("Saga compensation completed: sagaId={}", saga.getSagaId());
        return updated;
    }

    /**
     * Handle step failure
     */
    private RentalSaga handleStepFailure(RentalSaga saga, String stepName, String errorMessage) {
        saga.addStep(createFailureStep(stepName, errorMessage));
        return compensateSaga(saga, "Step failed: " + stepName + " - " + errorMessage);
    }

    /**
     * Compensate a specific step
     */
    private void compensateStep(RentalSaga saga, RentalSaga.SagaStep step) {
        log.info("Compensating step: sagaId={}, step={}", saga.getSagaId(), step.getStepName());

        String compensationAction = getCompensationAction(step.getStepName());

        try {
            // In a real implementation, this would call the appropriate service APIs
            // For now, we just log the compensation action
            log.info("Compensation executed: sagaId={}, action={}", saga.getSagaId(), compensationAction);
            step.setCompensationAction(compensationAction);

        } catch (Exception e) {
            log.error("Compensation failed: sagaId={}, step={}, error={}",
                    saga.getSagaId(), step.getStepName(), e.getMessage());
        }
    }

    /**
     * Get compensation action for a step
     */
    private String getCompensationAction(String stepName) {
        return switch (stepName) {
            case "VEHICLE_RESERVATION" -> "Release vehicle reservation";
            case "PAYMENT_PROCESSING" -> "Refund payment";
            case "VEHICLE_UNLOCK" -> "Lock vehicle";
            default -> "No compensation action defined";
        };
    }

    /**
     * Create a success step
     */
    private RentalSaga.SagaStep createSuccessStep(String stepName, SagaState state) {
        return RentalSaga.SagaStep.builder()
                .stepName(stepName)
                .state(state)
                .executedAt(LocalDateTime.now())
                .success(true)
                .build();
    }

    /**
     * Create a failure step
     */
    private RentalSaga.SagaStep createFailureStep(String stepName, String errorMessage) {
        return RentalSaga.SagaStep.builder()
                .stepName(stepName)
                .executedAt(LocalDateTime.now())
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * Retry a failed saga
     */
    public RentalSaga retrySaga(String sagaId) {
        RentalSaga saga = sagaRepository.findById(sagaId)
                .orElseThrow(() -> new IllegalArgumentException("Saga not found: " + sagaId));

        if (!saga.hasFailed()) {
            throw new IllegalStateException("Cannot retry non-failed saga: " + sagaId);
        }

        saga.setRetryCount(saga.getRetryCount() + 1);
        saga.setCurrentState(SagaState.STARTED);
        saga.setFailureReason(null);
        saga.getSteps().clear();

        log.info("Retrying saga: sagaId={}, retryCount={}", sagaId, saga.getRetryCount());
        return sagaRepository.save(saga);
    }
}
