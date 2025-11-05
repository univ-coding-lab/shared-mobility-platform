package com.next.common.event.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event published when vehicle battery level is critically low
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BatteryLowEvent extends BaseEvent {

    @JsonProperty("vehicleId")
    private String vehicleId;

    @JsonProperty("batteryLevel")
    private Integer batteryLevel;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("estimatedRangeKm")
    private Long estimatedRangeKm;

    public BatteryLowEvent(String vehicleId, Integer batteryLevel,
                            Double latitude, Double longitude, Long estimatedRangeKm) {
        super("BatteryLow", vehicleId);
        this.vehicleId = vehicleId;
        this.batteryLevel = batteryLevel;
        this.latitude = latitude;
        this.longitude = longitude;
        this.estimatedRangeKm = estimatedRangeKm;
    }
}
