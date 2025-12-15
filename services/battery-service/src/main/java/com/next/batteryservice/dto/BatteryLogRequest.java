package com.next.batteryservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatteryLogRequest {

    @NotBlank(message = "Vehicle ID is required")
    private String vehicleId;

    @NotNull(message = "Battery level is required")
    @Min(value = 0, message = "Battery level must be >= 0")
    @Max(value = 100, message = "Battery level must be <= 100")
    private Integer batteryLevel;

    private Double voltage;

    private Double current;

    private Double temperature;

    private Integer cycleCount;

    private String healthStatus;

    private Boolean isCharging;

    private Long estimatedRangeKm;

    private Long estimatedTimeToFullCharge;

    private String chargingStationId;

    private String source;
}
