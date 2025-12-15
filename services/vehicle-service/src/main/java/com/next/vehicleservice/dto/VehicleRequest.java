package com.next.vehicleservice.dto;

import com.next.common.domain.enums.VehicleType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequest {

    @NotBlank(message = "Serial number is required")
    private String serialNumber;

    @NotNull(message = "Vehicle type is required")
    private VehicleType type;

    @NotBlank(message = "Model name is required")
    private String model;

    @NotBlank(message = "Manufacturer is required")
    private String manufacturer;

    private Integer manufacturingYear;

    @Min(value = -90, message = "Latitude must be >= -90")
    @Max(value = 90, message = "Latitude must be <= 90")
    private Double latitude;

    @Min(value = -180, message = "Longitude must be >= -180")
    @Max(value = 180, message = "Longitude must be <= 180")
    private Double longitude;

    @Min(value = 0, message = "Battery level must be >= 0")
    @Max(value = 100, message = "Battery level must be <= 100")
    private Integer batteryLevel;

    private String iotDeviceId;

    private String iotProtocol;

    private String notes;
}
