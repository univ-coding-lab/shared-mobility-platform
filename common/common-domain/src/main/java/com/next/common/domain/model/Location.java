package com.next.common.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "location_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    @Id
    private String id;

    @Indexed
    private String vehicleId;

    @Indexed
    private LocalDateTime timestamp;

    private Double latitude;

    private Double longitude;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private double[] coordinates;

    private Double altitude;

    private Double speed;

    private Double heading;

    private Double accuracy;

    private String source;

    private Boolean isMoving;

    private String rentalId;

    public static Location of(String vehicleId, Double latitude, Double longitude) {
        Location location = new Location();
        location.setVehicleId(vehicleId);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setCoordinates(new double[]{longitude, latitude});
        location.setTimestamp(LocalDateTime.now());
        return location;
    }

    public void updateCoordinates() {
        if (latitude != null && longitude != null) {
            this.coordinates = new double[]{longitude, latitude};
        }
    }
}
