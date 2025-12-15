package com.next.rentalservice.saga.command;

import com.next.common.event.saga.SagaState;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Result wrapper for Saga command execution
 */
@Getter
@Builder
public class SagaCommandResult {

    private final String commandName;
    private final boolean success;
    private final SagaState resultState;
    private final LocalDateTime executedAt;
    private final String errorMessage;
    private final Map<String, Object> outputData;

    public static SagaCommandResult success(String commandName, SagaState state) {
        return SagaCommandResult.builder()
                .commandName(commandName)
                .success(true)
                .resultState(state)
                .executedAt(LocalDateTime.now())
                .build();
    }

    public static SagaCommandResult success(String commandName, SagaState state, Map<String, Object> data) {
        return SagaCommandResult.builder()
                .commandName(commandName)
                .success(true)
                .resultState(state)
                .executedAt(LocalDateTime.now())
                .outputData(data)
                .build();
    }

    public static SagaCommandResult failure(String commandName, String error) {
        return SagaCommandResult.builder()
                .commandName(commandName)
                .success(false)
                .errorMessage(error)
                .executedAt(LocalDateTime.now())
                .build();
    }
}
