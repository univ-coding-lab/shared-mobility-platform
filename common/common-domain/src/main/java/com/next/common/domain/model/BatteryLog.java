package com.next.common.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Battery log domain model for tracking vehicle battery status
 * Stored in MongoDB for time-series data
 */
@Document(collection = "battery_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatteryLog {

    @Id
    private String id;

    @Indexed
    private String vehicleId;

    @Indexed
    private LocalDateTime timestamp;

    @Indexed
    private Integer batteryLevel; // 0-100 percentage

    private Double voltage; // Volts

    private Double current; // Amperes

    private Double temperature; // Celsius

    private Integer cycleCount;

    private String healthStatus; // GOOD, FAIR, POOR, CRITICAL

    private Boolean isCharging;

    private Long estimatedRangeKm;

    private Long estimatedTimeToFullCharge; // minutes

    private String chargingStationId;

    private String source; // IOT_DEVICE, MANUAL, ESTIMATED

    /**
     * Check if battery is critically low
     */
    public boolean isCriticallyLow() {
        return batteryLevel != null && batteryLevel < 10;
    }

    /**
     * Check if battery needs charging
     */
    public boolean needsCharging() {
        return batteryLevel != null && batteryLevel < 20;
    }

    /**
     * Create battery log
     */
    public static BatteryLog of(String vehicleId, Integer level) {
        BatteryLog log = new BatteryLog();
        log.setVehicleId(vehicleId);
        log.setBatteryLevel(level);
        log.setTimestamp(LocalDateTime.now());
        log.setSource("IOT_DEVICE");
        return log;
    }
}
