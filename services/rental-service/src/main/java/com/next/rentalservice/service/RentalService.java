package com.next.rentalservice.service;

import com.next.common.domain.enums.RentalStatus;
import com.next.common.domain.exception.BusinessException;
import com.next.common.domain.exception.ResourceNotFoundException;
import com.next.common.domain.model.Rental;
import com.next.common.event.config.KafkaTopics;
import com.next.common.event.model.VehicleRentedEvent;
import com.next.common.event.model.VehicleReturnedEvent;
import com.next.common.event.publisher.EventPublisher;
import com.next.rentalservice.repository.RentalRepository;
import com.next.rentalservice.saga.RentalSaga;
import com.next.rentalservice.saga.RentalSagaOrchestrator;
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
    private final RentalSagaOrchestrator sagaOrchestrator;

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

        final Rental savedRental = rentalRepository.save(rental);
        log.info("Rental created: rentalId={}, vehicleId={}, userId={}", savedRental.getId(), vehicleId, userId);

        RentalSaga saga = sagaOrchestrator.executeSaga(savedRental.getId(), vehicleId, userId);

        if (saga.hasFailed()) {
            log.error("Saga failed: sagaId={}, rentalId={}, reason={}",
                    saga.getSagaId(), savedRental.getId(), saga.getFailureReason());
            throw new BusinessException("Rental saga failed: " + saga.getFailureReason());
        }

        log.info("Saga completed successfully: sagaId={}, rentalId={}", saga.getSagaId(), savedRental.getId());

        // Async event publishing - fire and forget with error logging
        VehicleRentedEvent event = new VehicleRentedEvent(vehicleId, userId, savedRental.getId(), lat, lon, batteryLevel);
        eventPublisher.publishAsync(KafkaTopics.VEHICLE_RENTED, vehicleId, event)
                .exceptionally(ex -> {
                    log.error("Failed to publish VehicleRentedEvent asynchronously: rentalId={}, vehicleId={}, error={}",
                            savedRental.getId(), vehicleId, ex.getMessage(), ex);
                    return null;
                });

        return savedRental;
    }

    @Transactional
    public Rental endRental(String rentalId, Double lat, Double lon, Integer batteryLevel) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental", rentalId));

        rental.complete(lat, lon, batteryLevel);
        final Rental completedRental = rentalRepository.save(rental);
        log.info("Rental completed: {}", completedRental.getId());

        // Async event publishing - fire and forget with error logging
        VehicleReturnedEvent event = new VehicleReturnedEvent(
                completedRental.getVehicleId(), completedRental.getUserId(), completedRental.getId(),
                lat, lon, batteryLevel, completedRental.getDistanceKm(), completedRental.getDurationMinutes()
        );
        eventPublisher.publishAsync(KafkaTopics.VEHICLE_RETURNED, completedRental.getVehicleId(), event)
                .exceptionally(ex -> {
                    log.error("Failed to publish VehicleReturnedEvent asynchronously: rentalId={}, vehicleId={}, error={}",
                            completedRental.getId(), completedRental.getVehicleId(), ex.getMessage(), ex);
                    return null;
                });

        return completedRental;
    }

    @Transactional(readOnly = true)
    public Rental getRental(String id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rental", id));
    }
}
