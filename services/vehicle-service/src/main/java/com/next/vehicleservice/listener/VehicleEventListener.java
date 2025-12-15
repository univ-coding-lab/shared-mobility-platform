package com.next.vehicleservice.listener;

import com.next.common.domain.enums.VehicleStatus;
import com.next.common.event.config.KafkaTopics;
import com.next.common.event.consumer.IdempotencyChecker;
import com.next.common.event.model.VehicleRentedEvent;
import com.next.common.event.model.VehicleReturnedEvent;
import com.next.vehicleservice.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleEventListener {

    private final VehicleService vehicleService;
    private final IdempotencyChecker idempotencyChecker;

    @KafkaListener(
            topics = KafkaTopics.VEHICLE_RENTED,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleVehicleRented(
            @Payload VehicleRentedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        log.info("Received VehicleRentedEvent: eventId={}, vehicleId={}, rentalId={}, partition={}, offset={}",
                event.getEventId(), event.getVehicleId(), event.getRentalId(), partition, offset);

        if (!idempotencyChecker.processIdempotently(event.getEventId())) {
            log.warn("Duplicate VehicleRentedEvent detected, skipping: eventId={}", event.getEventId());
            acknowledgment.acknowledge();
            return;
        }

        try {

            vehicleService.updateVehicleStatus(event.getVehicleId(), VehicleStatus.IN_USE);

            log.info("Successfully processed VehicleRentedEvent: vehicleId={} status updated to IN_USE",
                    event.getVehicleId());

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process VehicleRentedEvent: eventId={}, vehicleId={}, error={}",
                    event.getEventId(), event.getVehicleId(), e.getMessage(), e);

            throw e;
        }
    }

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
        log.info("Received VehicleReturnedEvent: eventId={}, vehicleId={}, rentalId={}, batteryLevel={}, partition={}, offset={}",
                event.getEventId(), event.getVehicleId(), event.getRentalId(), event.getBatteryLevel(), partition, offset);

        if (!idempotencyChecker.processIdempotently(event.getEventId())) {
            log.warn("Duplicate VehicleReturnedEvent detected, skipping: eventId={}", event.getEventId());
            acknowledgment.acknowledge();
            return;
        }

        try {

            vehicleService.updateVehicleStatus(event.getVehicleId(), VehicleStatus.AVAILABLE);

            log.info("Successfully processed VehicleReturnedEvent: vehicleId={} status updated to AVAILABLE",
                    event.getVehicleId());

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process VehicleReturnedEvent: eventId={}, vehicleId={}, error={}",
                    event.getEventId(), event.getVehicleId(), e.getMessage(), e);

            throw e;
        }
    }
}
