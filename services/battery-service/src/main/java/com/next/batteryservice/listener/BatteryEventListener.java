package com.next.batteryservice.listener;

import com.next.common.domain.model.BatteryLog;
import com.next.common.event.config.KafkaTopics;
import com.next.common.event.consumer.IdempotencyChecker;
import com.next.common.event.model.VehicleReturnedEvent;
import com.next.batteryservice.service.BatteryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka event listener for Battery Service
 * Handles battery monitoring and logging events
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatteryEventListener {

    private final BatteryService batteryService;
    private final IdempotencyChecker idempotencyChecker;

    /**
     * Handle VehicleReturnedEvent - Log battery level at rental end
     *
     * @param event VehicleReturnedEvent from Rental Service
     * @param partition Kafka partition number
     * @param offset Kafka offset
     * @param acknowledgment Manual acknowledgment for Kafka consumer
     */
    @KafkaListener(
            topics = KafkaTopics.VEHICLE_RETURNED,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleVehicleReturned(
            @Payload VehicleReturnedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        log.info("Received VehicleReturnedEvent: eventId={}, vehicleId={}, batteryLevel={}, partition={}, offset={}",
                event.getEventId(), event.getVehicleId(), event.getBatteryLevel(), partition, offset);

        // Check idempotency - prevent duplicate processing
        if (!idempotencyChecker.processIdempotently(event.getEventId())) {
            log.warn("Duplicate VehicleReturnedEvent detected, skipping: eventId={}", event.getEventId());
            acknowledgment.acknowledge();
            return;
        }

        try {
            // Create battery log for rental end
            BatteryLog batteryLog = BatteryLog.builder()
                    .vehicleId(event.getVehicleId())
                    .batteryLevel(event.getBatteryLevel())
                    .source("RENTAL_END")
                    .build();

            // Calculate estimated range (simple formula: 1% battery = 2km range)
            if (event.getBatteryLevel() != null) {
                batteryLog.setEstimatedRangeKm((long) (event.getBatteryLevel() * 2));
            }

            // Determine health status based on distance traveled and battery consumed
            if (event.getDistanceKm() != null && event.getBatteryLevel() != null) {
                batteryLog.setHealthStatus(determineHealthStatus(event.getDistanceKm(), event.getBatteryLevel()));
            }

            // Save battery log (will automatically publish BatteryLowEvent if battery < 20%)
            batteryService.saveBatteryLog(batteryLog);

            log.info("Successfully processed VehicleReturnedEvent: saved battery log for vehicleId={}, level={}%, estimatedRange={}km",
                    event.getVehicleId(), event.getBatteryLevel(), batteryLog.getEstimatedRangeKm());

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process VehicleReturnedEvent: eventId={}, vehicleId={}, error={}",
                    event.getEventId(), event.getVehicleId(), e.getMessage(), e);
            throw e; // Do NOT acknowledge - message will be redelivered
        }
    }

    /**
     * Determine battery health status based on usage patterns
     *
     * @param distanceKm Distance traveled
     * @param batteryLevel Remaining battery percentage
     * @return Health status string
     */
    private String determineHealthStatus(Double distanceKm, Integer batteryLevel) {
        if (batteryLevel >= 80) {
            return "GOOD";
        } else if (batteryLevel >= 50) {
            return "FAIR";
        } else if (batteryLevel >= 20) {
            return "POOR";
        } else {
            return "CRITICAL";
        }
    }
}
