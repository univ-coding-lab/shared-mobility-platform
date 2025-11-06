package com.next.rentalservice.service;

import com.next.common.domain.enums.RentalStatus;
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
        // Create rental record
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
        log.info("Rental created: rentalId={}, vehicleId={}, userId={}", rental.getId(), vehicleId, userId);

        // Start Saga orchestration for distributed transaction
        try {
            RentalSaga saga = sagaOrchestrator.startSaga(rental.getId(), vehicleId, userId);
            log.info("Saga started: sagaId={}, rentalId={}", saga.getSagaId(), rental.getId());

            // Execute saga steps
            saga = sagaOrchestrator.executeVehicleReservation(saga);
            saga = sagaOrchestrator.executePaymentProcessing(saga);
            saga = sagaOrchestrator.executeVehicleUnlock(saga);
            saga = sagaOrchestrator.completeSaga(saga);

            log.info("Saga completed: sagaId={}, rentalId={}", saga.getSagaId(), rental.getId());

        } catch (Exception e) {
            log.error("Saga execution failed: rentalId={}, error={}", rental.getId(), e.getMessage());
            // Saga orchestrator will handle compensation automatically
        }

        // Publish event to notify other services
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
