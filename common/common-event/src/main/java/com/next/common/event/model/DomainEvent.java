package com.next.common.event.model;

import java.time.LocalDateTime;

public interface DomainEvent {

    String getEventId();

    String getEventType();

    LocalDateTime getTimestamp();

    String getAggregateId();

    default String getVersion() {
        return "1.0";
    }
}
