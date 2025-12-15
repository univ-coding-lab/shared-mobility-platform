package com.next.rentalservice.saga.context;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Context object carrying all data needed for saga execution.
 * Thread-safe for potential async command execution.
 */
@Getter
@Builder
public class SagaContext {

    private final String sagaId;
    private final String rentalId;
    private final String vehicleId;
    private final String userId;

    @Builder.Default
    private final Map<String, Object> metadata = new ConcurrentHashMap<>();

    @Builder.Default
    private final Map<String, Object> stepResults = new ConcurrentHashMap<>();

    /**
     * Store a result from a command execution for use by subsequent commands.
     */
    public void putStepResult(String key, Object value) {
        stepResults.put(key, value);
    }

    /**
     * Retrieve a step result.
     */
    @SuppressWarnings("unchecked")
    public <T> T getStepResult(String key, Class<T> type) {
        Object value = stepResults.get(key);
        return value != null ? (T) value : null;
    }

    /**
     * Add metadata to the context.
     */
    public void putMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    /**
     * Retrieve metadata.
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> type) {
        Object value = metadata.get(key);
        return value != null ? (T) value : null;
    }
}
