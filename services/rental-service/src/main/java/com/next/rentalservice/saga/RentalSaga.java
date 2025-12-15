package com.next.rentalservice.saga;

import com.next.common.event.saga.SagaState;
import com.next.rentalservice.saga.model.SagaStepRecord;
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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "rental_saga", timeToLive = 86400)
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
    private List<SagaStepRecord> steps = new ArrayList<>();

    private String failureReason;

    private Integer retryCount;

    public void addStep(SagaStepRecord step) {
        if (steps == null) {
            steps = new ArrayList<>();
        }
        steps.add(step);
    }

    public SagaStepRecord getLastStep() {
        if (steps == null || steps.isEmpty()) {
            return null;
        }
        return steps.get(steps.size() - 1);
    }

    public boolean isInProgress() {
        return currentState != null && !currentState.isTerminal();
    }

    public boolean hasFailed() {
        return currentState == SagaState.FAILED;
    }

    public boolean isCompleted() {
        return currentState == SagaState.COMPLETED;
    }

    public List<String> getSuccessfulStepNames() {
        if (steps == null) {
            return List.of();
        }
        return steps.stream()
                .filter(SagaStepRecord::isSuccess)
                .filter(s -> !s.getStepName().endsWith("_COMPENSATION"))
                .map(SagaStepRecord::getStepName)
                .toList();
    }
}
