package com.next.common.event.publisher;

import com.next.common.event.model.DomainEvent;

import java.util.concurrent.CompletableFuture;

public interface EventPublisher {

    void publish(String topic, DomainEvent event);

    void publish(String topic, String key, DomainEvent event);

    CompletableFuture<Void> publishAsync(String topic, DomainEvent event);

    CompletableFuture<Void> publishAsync(String topic, String key, DomainEvent event);
}
