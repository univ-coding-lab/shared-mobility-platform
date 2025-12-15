package com.next.common.event.consumer;

import com.next.common.event.model.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;

import java.util.function.Consumer;

/**
 * Abstract base class for Kafka event listeners providing common functionality:
 * - Idempotent event processing
 * - Structured logging
 * - Error handling
 * - Manual acknowledgment
 *
 * Supports both single-event-type listeners (via inheritance) and
 * multi-event-type listeners (via the helper method with Consumer).
 *
 * @param <T> The type of DomainEvent this listener handles (optional, can use DomainEvent)
 */
@Slf4j
public abstract class AbstractEventListener<T extends DomainEvent> {

    protected final IdempotencyChecker idempotencyChecker;

    protected AbstractEventListener(IdempotencyChecker idempotencyChecker) {
        this.idempotencyChecker = idempotencyChecker;
    }

    /**
     * Template method for handling events with idempotency check and structured logging.
     * For listeners that handle a single event type.
     *
     * @param event the domain event to process
     * @param acknowledgment Kafka acknowledgment for manual commit
     * @param eventType descriptive name of the event type for logging
     */
    protected void handleEvent(T event, Acknowledgment acknowledgment, String eventType) {
        handleEventWithProcessor(event, acknowledgment, eventType, this::processEvent);
    }

    /**
     * Flexible method for handling any event type with a custom processor.
     * Useful for listeners that handle multiple event types.
     *
     * @param event the domain event to process
     * @param acknowledgment Kafka acknowledgment for manual commit
     * @param eventType descriptive name of the event type for logging
     * @param processor the processing logic for this specific event
     * @param <E> the specific event type
     */
    protected <E extends DomainEvent> void handleEventWithProcessor(
            E event,
            Acknowledgment acknowledgment,
            String eventType,
            Consumer<E> processor) {

        log.info("Received {} event: eventId={}, aggregateId={}",
                eventType, event.getEventId(), event.getAggregateId());

        if (!idempotencyChecker.processIdempotently(event.getEventId())) {
            log.warn("Duplicate {} event detected, skipping: eventId={}",
                    eventType, event.getEventId());
            acknowledgment.acknowledge();
            return;
        }

        try {
            processor.accept(event);
            acknowledgment.acknowledge();
            log.info("{} event processed successfully: eventId={}",
                    eventType, event.getEventId());
        } catch (Exception e) {
            log.error("Failed to process {} event: eventId={}, error={}",
                    eventType, event.getEventId(), e.getMessage(), e);
            // Re-throw to trigger DLQ handling
            throw e;
        }
    }

    /**
     * Process the event. Override this method for single-event-type listeners.
     *
     * @param event the domain event to process
     */
    protected void processEvent(T event) {
        // Default implementation - override in subclass or use handleEventWithProcessor
        throw new UnsupportedOperationException("Override processEvent or use handleEventWithProcessor");
    }
}
