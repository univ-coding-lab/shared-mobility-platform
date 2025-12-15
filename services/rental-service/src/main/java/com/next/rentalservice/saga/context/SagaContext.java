package com.next.rentalservice.saga.context;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    public void putStepResult(String key, Object value) {
        stepResults.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getStepResult(String key, Class<T> type) {
        Object value = stepResults.get(key);
        return value != null ? (T) value : null;
    }

    public void putMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> type) {
        Object value = metadata.get(key);
        return value != null ? (T) value : null;
    }
}
