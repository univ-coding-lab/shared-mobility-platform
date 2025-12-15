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

    public boolean processIdempotently(String eventId) {
        if (isProcessed(eventId)) {
            log.info("Skipping duplicate event: {}", eventId);
            return false;
        }

        return markAsProcessed(eventId);
    }
}
