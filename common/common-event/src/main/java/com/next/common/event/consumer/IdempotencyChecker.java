package com.next.common.event.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyChecker {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String EVENT_PREFIX = "event:processed:";
    private static final Duration TTL = Duration.ofDays(7);

    public boolean isProcessed(String eventId) {
        String key = EVENT_PREFIX + eventId;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    public boolean markAsProcessed(String eventId) {
        String key = EVENT_PREFIX + eventId;
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, "1", TTL);

        if (Boolean.TRUE.equals(result)) {
            log.debug("Marked event {} as processed", eventId);
            return true;
        }

        log.warn("Event {} was already processed (duplicate)", eventId);
        return false;
    }

    /**
     * Atomically checks if an event has been processed and marks it as processed if not.
     * Uses Redis setIfAbsent for thread-safe, race-condition-free operation.
     *
     * @param eventId the event ID to check and mark
     * @return true if this is the first time processing (caller should process),
     *         false if already processed (caller should skip)
     */
    public boolean processIdempotently(String eventId) {
        String key = EVENT_PREFIX + eventId;
        // setIfAbsent is atomic - performs check and set in a single operation
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, "1", TTL);

        if (Boolean.TRUE.equals(result)) {
            log.debug("Event marked for processing: {}", eventId);
            return true;
        }

        log.info("Duplicate event detected, skipping: {}", eventId);
        return false;
    }
}
