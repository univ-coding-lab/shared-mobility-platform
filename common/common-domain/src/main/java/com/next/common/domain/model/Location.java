package com.next.common.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Location domain model for tracking vehicle GPS positions
 * Stored in MongoDB for time-series data
 */
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
    private double[] coordinates; // [longitude, latitude] for MongoDB

    private Double altitude;

    private Double speed; // km/h

    private Double heading; // degrees from north

    private Double accuracy; // meters

    private String source; // GPS, NETWORK, CELLULAR, etc.

    private Boolean isMoving;

    private String rentalId; // If vehicle is in rental

    /**
     * Create Location from lat/lon
     */
    public static Location of(String vehicleId, Double latitude, Double longitude) {
        Location location = new Location();
        location.setVehicleId(vehicleId);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setCoordinates(new double[]{longitude, latitude}); // MongoDB GeoJSON format
        location.setTimestamp(LocalDateTime.now());
        return location;
    }

    /**
     * Update coordinates array when lat/lon changes
     */
    public void updateCoordinates() {
        if (latitude != null && longitude != null) {
            this.coordinates = new double[]{longitude, latitude};
        }
    }
}
