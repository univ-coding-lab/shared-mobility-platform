package com.next.common.event.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = VehicleRentedEvent.class, name = "VehicleRentedEvent"),
    @JsonSubTypes.Type(value = VehicleReturnedEvent.class, name = "VehicleReturnedEvent"),
    @JsonSubTypes.Type(value = VehicleMovedEvent.class, name = "VehicleMovedEvent"),
    @JsonSubTypes.Type(value = BatteryLowEvent.class, name = "BatteryLowEvent")
})
public abstract class BaseEvent implements DomainEvent {

    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("aggregateId")
    private String aggregateId;

    @JsonProperty("version")
    private String version;

    protected BaseEvent(String eventType, String aggregateId) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.timestamp = LocalDateTime.now();
        this.version = "1.0";
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public String getEventType() {
        return eventType;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    @Override
    public String getVersion() {
        return version != null ? version : "1.0";
    }
}
