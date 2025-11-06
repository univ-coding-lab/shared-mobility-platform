package com.next.rentalservice.saga;

import com.next.common.event.saga.SagaState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Rental Saga data model for tracking distributed transaction state
 * Stored in Redis for fast access and automatic expiration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "rental_saga", timeToLive = 86400) // 24 hour TTL
public class RentalSaga {

    @Id
    private String sagaId;

    @Indexed
    private String rentalId;

    @Indexed
    private String vehicleId;

    private String userId;

    private SagaState currentState;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Builder.Default
    private List<SagaStep> steps = new ArrayList<>();

    private String failureReason;

    private Integer retryCount;

    /**
     * Add a step to the saga
     */
    public void addStep(SagaStep step) {
        if (steps == null) {
            steps = new ArrayList<>();
        }
        steps.add(step);
    }

    /**
     * Get the last step
     */
    public SagaStep getLastStep() {
        if (steps == null || steps.isEmpty()) {
            return null;
        }
        return steps.get(steps.size() - 1);
    }

    /**
     * Check if saga is in progress
     */
    public boolean isInProgress() {
        return currentState != null && !currentState.isTerminal();
    }

    /**
     * Check if saga has failed
     */
    public boolean hasFailed() {
        return currentState == SagaState.FAILED;
    }

    /**
     * Check if saga is completed successfully
     */
    public boolean isCompleted() {
        return currentState == SagaState.COMPLETED;
    }

    /**
     * Inner class representing a saga step
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SagaStep {
        private String stepName;
        private SagaState state;
        private LocalDateTime executedAt;
        private boolean success;
        private String errorMessage;
        private String compensationAction;
    }
}
