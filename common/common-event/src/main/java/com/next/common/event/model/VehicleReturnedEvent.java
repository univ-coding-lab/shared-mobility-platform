package com.next.common.event.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a vehicle is returned
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VehicleReturnedEvent extends BaseEvent {

    @JsonProperty("vehicleId")
    private String vehicleId;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("rentalId")
    private String rentalId;

    @JsonProperty("endLatitude")
    private Double endLatitude;

    @JsonProperty("endLongitude")
    private Double endLongitude;

    @JsonProperty("batteryLevel")
    private Integer batteryLevel;

    @JsonProperty("distanceKm")
    private Double distanceKm;

    @JsonProperty("durationMinutes")
    private Long durationMinutes;

    public VehicleReturnedEvent(String vehicleId, String userId, String rentalId,
                                 Double endLatitude, Double endLongitude, Integer batteryLevel,
                                 Double distanceKm, Long durationMinutes) {
        super("VehicleReturned", vehicleId);
        this.vehicleId = vehicleId;
        this.userId = userId;
        this.rentalId = rentalId;
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
        this.batteryLevel = batteryLevel;
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
    }
}
