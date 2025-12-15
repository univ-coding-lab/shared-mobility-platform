package com.next.batteryservice.listener;

import com.next.common.domain.constants.BatteryConstants;
import com.next.common.domain.model.BatteryLog;
import com.next.common.event.config.KafkaTopics;
import com.next.common.event.consumer.AbstractEventListener;
import com.next.common.event.consumer.IdempotencyChecker;
import com.next.common.event.model.DomainEvent;
import com.next.common.event.model.VehicleReturnedEvent;
import com.next.batteryservice.service.BatteryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BatteryEventListener extends AbstractEventListener<DomainEvent> {

    private final BatteryService batteryService;

    public BatteryEventListener(IdempotencyChecker idempotencyChecker, BatteryService batteryService) {
        super(idempotencyChecker);
        this.batteryService = batteryService;
    }

    @KafkaListener(
            topics = KafkaTopics.VEHICLE_RETURNED,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleVehicleReturned(@Payload VehicleReturnedEvent event, Acknowledgment acknowledgment) {
        handleEventWithProcessor(event, acknowledgment, "VehicleReturned", this::processVehicleReturned);
    }

    private void processVehicleReturned(VehicleReturnedEvent event) {
        BatteryLog batteryLog = BatteryLog.builder()
                .vehicleId(event.getVehicleId())
                .batteryLevel(event.getBatteryLevel())
                .source("RENTAL_END")
                .build();

        if (event.getBatteryLevel() != null) {
            batteryLog.setEstimatedRangeKm((long) (event.getBatteryLevel() * BatteryConstants.RANGE_ESTIMATION_FACTOR));
        }

        if (event.getDistanceKm() != null && event.getBatteryLevel() != null) {
            batteryLog.setHealthStatus(determineHealthStatus(event.getBatteryLevel()));
        }

        batteryService.saveBatteryLog(batteryLog);

        log.debug("Battery log saved: vehicleId={}, level={}%, estimatedRange={}km",
                event.getVehicleId(), event.getBatteryLevel(), batteryLog.getEstimatedRangeKm());
    }

    private String determineHealthStatus(Integer batteryLevel) {
        if (batteryLevel >= BatteryConstants.HEALTH_GOOD_THRESHOLD) {
            return "GOOD";
        } else if (batteryLevel >= BatteryConstants.HEALTH_FAIR_THRESHOLD) {
            return "FAIR";
        } else if (batteryLevel >= BatteryConstants.HEALTH_POOR_THRESHOLD) {
            return "POOR";
        } else {
            return "CRITICAL";
        }
    }
}
