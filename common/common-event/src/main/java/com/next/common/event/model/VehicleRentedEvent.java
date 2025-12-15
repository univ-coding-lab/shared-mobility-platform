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
public class VehicleRentedEvent extends BaseEvent {

    @JsonProperty("vehicleId")
    private String vehicleId;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("rentalId")
    private String rentalId;

    @JsonProperty("startLatitude")
    private Double startLatitude;

    @JsonProperty("startLongitude")
    private Double startLongitude;

    @JsonProperty("batteryLevel")
    private Integer batteryLevel;

    public VehicleRentedEvent(String vehicleId, String userId, String rentalId,
                               Double startLatitude, Double startLongitude, Integer batteryLevel) {
        super("VehicleRented", vehicleId);
        this.vehicleId = vehicleId;
        this.userId = userId;
        this.rentalId = rentalId;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.batteryLevel = batteryLevel;
    }
}
