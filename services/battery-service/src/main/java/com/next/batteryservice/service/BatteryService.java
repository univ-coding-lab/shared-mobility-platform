package com.next.batteryservice.service;

import com.next.common.domain.model.BatteryLog;
import com.next.common.event.config.KafkaTopics;
import com.next.common.event.model.BatteryLowEvent;
import com.next.common.event.publisher.EventPublisher;
import com.next.batteryservice.repository.BatteryLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatteryService {
    private final BatteryLogRepository batteryLogRepository;
    private final EventPublisher eventPublisher;

    public BatteryLog saveBatteryLog(BatteryLog batteryLog) {
        BatteryLog saved = batteryLogRepository.save(batteryLog);
        log.info("Battery log saved for vehicle: {}", batteryLog.getVehicleId());

        if (batteryLog.needsCharging()) {
            BatteryLowEvent event = new BatteryLowEvent(
                    batteryLog.getVehicleId(),
                    batteryLog.getBatteryLevel(),
                    null, null,
                    batteryLog.getEstimatedRangeKm()
            );
            eventPublisher.publish(KafkaTopics.BATTERY_LOW, batteryLog.getVehicleId(), event);
            log.warn("Battery low event published for vehicle: {}", batteryLog.getVehicleId());
        }

        return saved;
    }

    public BatteryLog getLatestBatteryLog(String vehicleId) {
        return batteryLogRepository.findTopByVehicleIdOrderByTimestampDesc(vehicleId);
    }
}
