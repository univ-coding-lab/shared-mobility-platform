package com.next.locationservice.dto;

import com.next.common.domain.model.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponse {

    private String id;
    private String vehicleId;
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private Double speed;
    private Double heading;
    private Double accuracy;
    private String source;
    private Boolean isMoving;
    private String rentalId;
    private LocalDateTime timestamp;

    public static LocationResponse from(Location location) {
        return LocationResponse.builder()
                .id(location.getId())
                .vehicleId(location.getVehicleId())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .altitude(location.getAltitude())
                .speed(location.getSpeed())
                .heading(location.getHeading())
                .accuracy(location.getAccuracy())
                .source(location.getSource())
                .isMoving(location.getIsMoving())
                .rentalId(location.getRentalId())
                .timestamp(location.getTimestamp())
                .build();
    }
}
