package com.next.common.event.consumer;

import com.next.common.event.model.DomainEvent;

/**
 * Interface for event listeners to implement
 * Services should implement this interface to handle specific events
 *
 * @param <T> Type of domain event to handle
 */
public interface EventListener<T extends DomainEvent> {

    /**
     * Handle the domain event
     *
     * @param event Domain event to process
     */
    void handle(T event);

    /**
     * Get the event type this listener handles
     *
     * @return Event type class
     */
    Class<T> getEventType();

    /**
     * Check if this listener can handle the event
     *
     * @param event Event to check
     * @return true if this listener can handle the event
     */
    default boolean canHandle(DomainEvent event) {
        return getEventType().isInstance(event);
    }
}
