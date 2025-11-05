package com.next.common.domain.model;

import com.next.common.domain.enums.VehicleStatus;
import com.next.common.domain.enums.VehicleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Vehicle domain model representing a shared mobility vehicle
 */
@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle extends BaseEntity {

    @NotBlank(message = "Vehicle serial number is required")
    @Column(name = "serial_number", unique = true, nullable = false)
    private String serialNumber;

    @NotNull(message = "Vehicle type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private VehicleType type;

    @NotNull(message = "Vehicle status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VehicleStatus status;

    @NotBlank(message = "Model name is required")
    @Column(name = "model", nullable = false)
    private String model;

    @NotBlank(message = "Manufacturer is required")
    @Column(name = "manufacturer", nullable = false)
    private String manufacturer;

    @Column(name = "manufacturing_year")
    private Integer manufacturingYear;

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Column(name = "last_known_latitude")
    private Double lastKnownLatitude;

    @Column(name = "last_known_longitude")
    private Double lastKnownLongitude;

    @Column(name = "last_location_update")
    private java.time.LocalDateTime lastLocationUpdate;

    @Column(name = "iot_device_id", unique = true)
    private String iotDeviceId;

    @Column(name = "iot_protocol")
    private String iotProtocol;

    @Column(name = "total_distance_km")
    private Double totalDistanceKm;

    @Column(name = "total_rides")
    private Long totalRides;

    @Column(name = "notes", length = 1000)
    private String notes;

    /**
     * Check if vehicle is available for rental
     */
    public boolean isAvailable() {
        return status == VehicleStatus.AVAILABLE && batteryLevel != null && batteryLevel > 20;
    }

    /**
     * Update vehicle location
     */
    public void updateLocation(Double latitude, Double longitude) {
        this.lastKnownLatitude = latitude;
        this.lastKnownLongitude = longitude;
        this.lastLocationUpdate = java.time.LocalDateTime.now();
    }

    /**
     * Update battery level
     */
    public void updateBatteryLevel(Integer level) {
        this.batteryLevel = level;
    }
}
