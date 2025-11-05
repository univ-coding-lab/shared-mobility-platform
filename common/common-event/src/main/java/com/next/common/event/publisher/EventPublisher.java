package com.next.common.event.publisher;

import com.next.common.event.model.DomainEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for publishing domain events to the event bus
 */
public interface EventPublisher {

    /**
     * Publish an event to a specific topic
     *
     * @param topic Kafka topic name
     * @param event Domain event to publish
     */
    void publish(String topic, DomainEvent event);

    /**
     * Publish an event with a specific partition key
     * Events with the same key will go to the same partition for ordering
     *
     * @param topic Kafka topic name
     * @param key Partition key (e.g., vehicleId)
     * @param event Domain event to publish
     */
    void publish(String topic, String key, DomainEvent event);

    /**
     * Publish an event asynchronously
     *
     * @param topic Kafka topic name
     * @param event Domain event to publish
     * @return CompletableFuture for async handling
     */
    CompletableFuture<Void> publishAsync(String topic, DomainEvent event);

    /**
     * Publish an event asynchronously with partition key
     *
     * @param topic Kafka topic name
     * @param key Partition key
     * @param event Domain event to publish
     * @return CompletableFuture for async handling
     */
    CompletableFuture<Void> publishAsync(String topic, String key, DomainEvent event);
}
