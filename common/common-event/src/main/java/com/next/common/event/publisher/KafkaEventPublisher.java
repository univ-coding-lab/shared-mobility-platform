package com.next.common.event.publisher;

import com.next.common.event.model.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka implementation of EventPublisher
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;

    @Override
    public void publish(String topic, DomainEvent event) {
        try {
            kafkaTemplate.send(topic, event).get();
            log.info("Published event {} to topic {}", event.getEventType(), topic);
        } catch (Exception e) {
            log.error("Failed to publish event {} to topic {}: {}",
                     event.getEventType(), topic, e.getMessage(), e);
            throw new RuntimeException("Event publication failed", e);
        }
    }

    @Override
    public void publish(String topic, String key, DomainEvent event) {
        try {
            kafkaTemplate.send(topic, key, event).get();
            log.info("Published event {} to topic {} with key {}",
                    event.getEventType(), topic, key);
        } catch (Exception e) {
            log.error("Failed to publish event {} to topic {} with key {}: {}",
                     event.getEventType(), topic, key, e.getMessage(), e);
            throw new RuntimeException("Event publication failed", e);
        }
    }

    @Override
    public CompletableFuture<Void> publishAsync(String topic, DomainEvent event) {
        CompletableFuture<SendResult<String, DomainEvent>> future = kafkaTemplate.send(topic, event);

        return future.handle((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event {} to topic {}: {}",
                         event.getEventType(), topic, ex.getMessage(), ex);
                throw new RuntimeException("Async event publication failed", ex);
            }
            log.info("Async published event {} to topic {}", event.getEventType(), topic);
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> publishAsync(String topic, String key, DomainEvent event) {
        CompletableFuture<SendResult<String, DomainEvent>> future =
                kafkaTemplate.send(topic, key, event);

        return future.handle((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event {} to topic {} with key {}: {}",
                         event.getEventType(), topic, key, ex.getMessage(), ex);
                throw new RuntimeException("Async event publication failed", ex);
            }
            log.info("Async published event {} to topic {} with key {}",
                    event.getEventType(), topic, key);
            return null;
        });
    }
}
