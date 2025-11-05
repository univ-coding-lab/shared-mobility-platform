package com.next.rentalservice.service;

import com.next.common.domain.enums.RentalStatus;
import com.next.common.domain.exception.ResourceNotFoundException;
import com.next.common.domain.model.Rental;
import com.next.common.event.config.KafkaTopics;
import com.next.common.event.model.VehicleRentedEvent;
import com.next.common.event.model.VehicleReturnedEvent;
import com.next.common.event.publisher.EventPublisher;
import com.next.rentalservice.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalService {
    private final RentalRepository rentalRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public Rental startRental(String userId, String vehicleId, Double lat, Double lon, Integer batteryLevel) {
        Rental rental = Rental.builder()
                .userId(userId)
                .vehicleId(vehicleId)
                .status(RentalStatus.ACTIVE)
                .startTime(LocalDateTime.now())
                .startLatitude(lat)
                .startLongitude(lon)
                .startBatteryLevel(batteryLevel)
                .build();

        rental = rentalRepository.save(rental);
        log.info("Rental started: {}", rental.getId());

        VehicleRentedEvent event = new VehicleRentedEvent(vehicleId, userId, rental.getId(), lat, lon, batteryLevel);
        eventPublisher.publish(KafkaTopics.VEHICLE_RENTED, vehicleId, event);

        return rental;
    }

    @Transactional
    public Rental endRental(String rentalId, Double lat, Double lon, Integer batteryLevel) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental", rentalId));

        rental.complete(lat, lon, batteryLevel);
        rental = rentalRepository.save(rental);
        log.info("Rental completed: {}", rental.getId());

        VehicleReturnedEvent event = new VehicleReturnedEvent(
                rental.getVehicleId(), rental.getUserId(), rental.getId(),
                lat, lon, batteryLevel, rental.getDistanceKm(), rental.getDurationMinutes()
        );
        eventPublisher.publish(KafkaTopics.VEHICLE_RETURNED, rental.getVehicleId(), event);

        return rental;
    }

    @Transactional(readOnly = true)
    public Rental getRental(String id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rental", id));
    }
}
