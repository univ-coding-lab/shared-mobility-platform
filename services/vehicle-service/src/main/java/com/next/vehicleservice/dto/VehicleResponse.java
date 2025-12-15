package com.next.vehicleservice.dto;

import com.next.common.domain.enums.VehicleStatus;
import com.next.common.domain.enums.VehicleType;
import com.next.common.domain.model.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {

    private String id;
    private String serialNumber;
    private VehicleType type;
    private VehicleStatus status;
    private String model;
    private String manufacturer;
    private Integer manufacturingYear;
    private Integer batteryLevel;
    private Double latitude;
    private Double longitude;
    private LocalDateTime lastLocationUpdate;
    private String iotDeviceId;
    private Double totalDistanceKm;
    private Long totalRides;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VehicleResponse from(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .serialNumber(vehicle.getSerialNumber())
                .type(vehicle.getType())
                .status(vehicle.getStatus())
                .model(vehicle.getModel())
                .manufacturer(vehicle.getManufacturer())
                .manufacturingYear(vehicle.getManufacturingYear())
                .batteryLevel(vehicle.getBatteryLevel())
                .latitude(vehicle.getLastKnownLatitude())
                .longitude(vehicle.getLastKnownLongitude())
                .lastLocationUpdate(vehicle.getLastLocationUpdate())
                .iotDeviceId(vehicle.getIotDeviceId())
                .totalDistanceKm(vehicle.getTotalDistanceKm())
                .totalRides(vehicle.getTotalRides())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}
