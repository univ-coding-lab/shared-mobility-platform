package com.next.rentalservice.saga.model;

import com.next.common.event.saga.SagaState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Record of a saga step execution
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaStepRecord {

    private String stepName;
    private SagaState state;
    private LocalDateTime executedAt;
    private boolean success;
    private String errorMessage;
    private String compensationAction;
}
