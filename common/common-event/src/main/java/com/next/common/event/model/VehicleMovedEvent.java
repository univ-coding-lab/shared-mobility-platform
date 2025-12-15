package com.next.common.event.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VehicleMovedEvent extends BaseEvent {

    @JsonProperty("vehicleId")
    private String vehicleId;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("speed")
    private Double speed;

    @JsonProperty("heading")
    private Double heading;

    @JsonProperty("isMoving")
    private Boolean isMoving;

    @JsonProperty("rentalId")
    private String rentalId;

    public VehicleMovedEvent(String vehicleId, Double latitude, Double longitude,
                              Double speed, Double heading, Boolean isMoving, String rentalId) {
        super("VehicleMoved", vehicleId);
        this.vehicleId = vehicleId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.heading = heading;
        this.isMoving = isMoving;
        this.rentalId = rentalId;
    }
}
