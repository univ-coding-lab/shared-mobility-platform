package com.next.locationservice.listener;

import com.next.common.domain.model.Location;
import com.next.common.event.config.KafkaTopics;
import com.next.common.event.consumer.AbstractEventListener;
import com.next.common.event.consumer.IdempotencyChecker;
import com.next.common.event.model.DomainEvent;
import com.next.common.event.model.VehicleMovedEvent;
import com.next.common.event.model.VehicleRentedEvent;
import com.next.common.event.model.VehicleReturnedEvent;
import com.next.locationservice.service.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LocationEventListener extends AbstractEventListener<DomainEvent> {

    private final LocationService locationService;

    public LocationEventListener(IdempotencyChecker idempotencyChecker, LocationService locationService) {
        super(idempotencyChecker);
        this.locationService = locationService;
    }

    @KafkaListener(
            topics = KafkaTopics.VEHICLE_RENTED,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleVehicleRented(@Payload VehicleRentedEvent event, Acknowledgment acknowledgment) {
        handleEventWithProcessor(event, acknowledgment, "VehicleRented", this::processVehicleRented);
    }

    @KafkaListener(
            topics = KafkaTopics.VEHICLE_RETURNED,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleVehicleReturned(@Payload VehicleReturnedEvent event, Acknowledgment acknowledgment) {
        handleEventWithProcessor(event, acknowledgment, "VehicleReturned", this::processVehicleReturned);
    }

    @KafkaListener(
            topics = KafkaTopics.VEHICLE_MOVED,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleVehicleMoved(@Payload VehicleMovedEvent event, Acknowledgment acknowledgment) {
        handleEventWithProcessor(event, acknowledgment, "VehicleMoved", this::processVehicleMoved);
    }

    private void processVehicleRented(VehicleRentedEvent event) {
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

        log.debug("Start location saved: vehicleId={}, rentalId={}",
                event.getVehicleId(), event.getRentalId());
    }

    private void processVehicleReturned(VehicleReturnedEvent event) {
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

        log.debug("End location saved: vehicleId={}, rentalId={}, distance={}km",
                event.getVehicleId(), event.getRentalId(), event.getDistanceKm());
    }

    private void processVehicleMoved(VehicleMovedEvent event) {
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

        log.trace("GPS location saved: vehicleId={}, speed={}km/h",
                event.getVehicleId(), event.getSpeed());
    }
}
