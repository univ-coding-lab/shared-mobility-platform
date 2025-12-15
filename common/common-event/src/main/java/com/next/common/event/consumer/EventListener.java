package com.next.common.event.consumer;

import com.next.common.event.model.DomainEvent;

public interface EventListener<T extends DomainEvent> {

    void handle(T event);

    Class<T> getEventType();

    default boolean canHandle(DomainEvent event) {
        return getEventType().isInstance(event);
    }
}
