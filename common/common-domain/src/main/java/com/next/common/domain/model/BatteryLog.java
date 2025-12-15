package com.next.common.domain.model;

import com.next.common.domain.constants.BatteryConstants;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

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

    public boolean isCriticallyLow() {
        return batteryLevel != null && batteryLevel < BatteryConstants.CRITICALLY_LOW_THRESHOLD;
    }

    public boolean needsCharging() {
        return batteryLevel != null && batteryLevel < BatteryConstants.NEEDS_CHARGING_THRESHOLD;
    }

    public static BatteryLog of(String vehicleId, Integer level) {
        BatteryLog log = new BatteryLog();
        log.setVehicleId(vehicleId);
        log.setBatteryLevel(level);
        log.setTimestamp(LocalDateTime.now());
        log.setSource("IOT_DEVICE");
        return log;
    }
}
