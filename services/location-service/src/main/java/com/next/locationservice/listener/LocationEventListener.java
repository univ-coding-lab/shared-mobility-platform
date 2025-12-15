package com.next.locationservice.listener;

import com.next.common.domain.model.Location;
import com.next.common.event.config.KafkaTopics;
import com.next.common.event.consumer.IdempotencyChecker;
import com.next.common.event.model.VehicleMovedEvent;
import com.next.common.event.model.VehicleRentedEvent;
import com.next.common.event.model.VehicleReturnedEvent;
import com.next.locationservice.service.LocationService;
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
public class LocationEventListener {

    private final LocationService locationService;
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

            Location location = Location.builder()
                    .vehicleId(event.getVehicleId())
                    .latitude(event.getStartLatitude())
                    .longitude(event.getStartLongitude())
                    .coordinates(new double[]{event.getStartLongitude(), event.getStartLatitude()})
                    .rentalId(event.getRentalId())
                    .source("RENTAL_START")
                    .isMoving(false)
                    .build();

            locationService.saveLocation(location);

            log.info("Successfully processed VehicleRentedEvent: saved start location for vehicleId={}, rentalId={}",
                    event.getVehicleId(), event.getRentalId());

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
        log.info("Received VehicleReturnedEvent: eventId={}, vehicleId={}, rentalId={}, partition={}, offset={}",
                event.getEventId(), event.getVehicleId(), event.getRentalId(), partition, offset);

        if (!idempotencyChecker.processIdempotently(event.getEventId())) {
            log.warn("Duplicate VehicleReturnedEvent detected, skipping: eventId={}", event.getEventId());
            acknowledgment.acknowledge();
            return;
        }

        try {

            Location location = Location.builder()
                    .vehicleId(event.getVehicleId())
                    .latitude(event.getEndLatitude())
                    .longitude(event.getEndLongitude())
                    .coordinates(new double[]{event.getEndLongitude(), event.getEndLatitude()})
                    .rentalId(event.getRentalId())
                    .source("RENTAL_END")
                    .isMoving(false)
                    .build();

            locationService.saveLocation(location);

            log.info("Successfully processed VehicleReturnedEvent: saved end location for vehicleId={}, rentalId={}, distance={}km, duration={}min",
                    event.getVehicleId(), event.getRentalId(), event.getDistanceKm(), event.getDurationMinutes());

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process VehicleReturnedEvent: eventId={}, vehicleId={}, error={}",
                    event.getEventId(), event.getVehicleId(), e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(
            topics = KafkaTopics.VEHICLE_MOVED,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleVehicleMoved(
            @Payload VehicleMovedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        log.debug("Received VehicleMovedEvent: eventId={}, vehicleId={}, lat={}, lon={}, partition={}, offset={}",
                event.getEventId(), event.getVehicleId(), event.getLatitude(), event.getLongitude(), partition, offset);

        if (!idempotencyChecker.processIdempotently(event.getEventId())) {
            log.debug("Duplicate VehicleMovedEvent detected, skipping: eventId={}", event.getEventId());
            acknowledgment.acknowledge();
            return;
        }

        try {

            Location location = Location.builder()
                    .vehicleId(event.getVehicleId())
                    .latitude(event.getLatitude())
                    .longitude(event.getLongitude())
                    .coordinates(new double[]{event.getLongitude(), event.getLatitude()})
                    .speed(event.getSpeed())
                    .heading(event.getHeading())
                    .isMoving(event.getIsMoving())
                    .rentalId(event.getRentalId())
                    .source("GPS")
                    .build();

            locationService.saveLocation(location);

            log.debug("Successfully processed VehicleMovedEvent: saved location for vehicleId={}, speed={}km/h",
                    event.getVehicleId(), event.getSpeed());

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process VehicleMovedEvent: eventId={}, vehicleId={}, error={}",
                    event.getEventId(), event.getVehicleId(), e.getMessage(), e);
            throw e;
        }
    }
}
