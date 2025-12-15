package com.next.batteryservice.dto;

import com.next.common.domain.model.BatteryLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatteryLogResponse {

    private String id;
    private String vehicleId;
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
    private Boolean isCriticallyLow;
    private Boolean needsCharging;
    private LocalDateTime timestamp;

    public static BatteryLogResponse from(BatteryLog log) {
        return BatteryLogResponse.builder()
                .id(log.getId())
                .vehicleId(log.getVehicleId())
                .batteryLevel(log.getBatteryLevel())
                .voltage(log.getVoltage())
                .current(log.getCurrent())
                .temperature(log.getTemperature())
                .cycleCount(log.getCycleCount())
                .healthStatus(log.getHealthStatus())
                .isCharging(log.getIsCharging())
                .estimatedRangeKm(log.getEstimatedRangeKm())
                .estimatedTimeToFullCharge(log.getEstimatedTimeToFullCharge())
                .chargingStationId(log.getChargingStationId())
                .source(log.getSource())
                .isCriticallyLow(log.isCriticallyLow())
                .needsCharging(log.needsCharging())
                .timestamp(log.getTimestamp())
                .build();
    }
}
