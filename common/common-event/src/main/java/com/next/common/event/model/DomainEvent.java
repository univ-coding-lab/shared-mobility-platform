package com.next.common.event.model;

import java.time.LocalDateTime;

/**
 * Base interface for all domain events
 */
public interface DomainEvent {

    /**
     * Get unique event ID
     */
    String getEventId();

    /**
     * Get event type
     */
    String getEventType();

    /**
     * Get timestamp when event occurred
     */
    LocalDateTime getTimestamp();

    /**
     * Get aggregate ID (entity ID that event relates to)
     */
    String getAggregateId();

    /**
     * Get event version for schema evolution
     */
    default String getVersion() {
        return "1.0";
    }
}
