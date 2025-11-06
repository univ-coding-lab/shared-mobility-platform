package com.next.iotsimulator.service;

import com.next.iotsimulator.model.SimulatedVehicle;
import com.next.iotsimulator.publisher.TelemetryPublisher;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Vehicle simulator service
 * Manages simulated vehicles and updates their telemetry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleSimulator {

    private final TelemetryPublisher telemetryPublisher;
    private final List<SimulatedVehicle> vehicles = new ArrayList<>();
    private final Random random = new Random();

    @Value("${simulator.vehicle.count:10}")
    private int vehicleCount;

    @Value("${simulator.update.interval:10000}")
    private long updateInterval;

    @Value("${simulator.moving.probability:0.3}")
    private double movingProbability;

    /**
     * Initialize simulated vehicles on startup
     */
    @PostConstruct
    public void initialize() {
        log.info("Initializing IoT Simulator with {} vehicles", vehicleCount);

        for (int i = 1; i <= vehicleCount; i++) {
            String vehicleId = "vehicle-" + i;
            SimulatedVehicle vehicle = SimulatedVehicle.createRandom(vehicleId);
            vehicles.add(vehicle);
            log.info("Created simulated vehicle: id={}, lat={}, lon={}, battery={}%",
                    vehicleId, vehicle.getLatitude(), vehicle.getLongitude(), vehicle.getBatteryLevel());
        }

        log.info("IoT Simulator initialized successfully with {} vehicles", vehicles.size());
    }

    /**
     * Update vehicle telemetry periodically
     * Runs every N seconds (configured by simulator.update.interval)
     */
    @Scheduled(fixedDelayString = "${simulator.update.interval:10000}")
    public void updateVehicles() {
        log.debug("Updating telemetry for {} vehicles", vehicles.size());

        for (SimulatedVehicle vehicle : vehicles) {
            try {
                // Randomly start/stop vehicle movement
                if (!vehicle.isMoving() && random.nextDouble() < movingProbability) {
                    vehicle.startRental("rental-" + System.currentTimeMillis());
                    log.info("Vehicle {} started moving (rental started)", vehicle.getVehicleId());
                } else if (vehicle.isMoving() && random.nextDouble() < 0.1) {
                    // 10% chance to stop
                    vehicle.endRental();
                    log.info("Vehicle {} stopped (rental ended)", vehicle.getVehicleId());
                }

                // Move vehicle if it's in motion
                if (vehicle.isMoving()) {
                    vehicle.move();
                }

                // Publish telemetry to Kafka and services
                telemetryPublisher.publishTelemetry(vehicle);

                // Log battery warnings
                if (vehicle.needsCharging() && !vehicle.isCriticallyLow()) {
                    log.warn("Vehicle {} battery low: {}%", vehicle.getVehicleId(), vehicle.getBatteryLevel());
                } else if (vehicle.isCriticallyLow()) {
                    log.error("Vehicle {} battery critically low: {}%", vehicle.getVehicleId(), vehicle.getBatteryLevel());
                }

            } catch (Exception e) {
                log.error("Failed to update vehicle {}: {}", vehicle.getVehicleId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Charge vehicles periodically (simulate charging stations)
     */
    @Scheduled(fixedRate = 60000) // Every minute
    public void chargeVehicles() {
        for (SimulatedVehicle vehicle : vehicles) {
            if (!vehicle.isMoving() && vehicle.getBatteryLevel() < 80) {
                // Charge stationary vehicles by 10%
                vehicle.charge(10);
                log.debug("Charged vehicle {} to {}%", vehicle.getVehicleId(), vehicle.getBatteryLevel());
            }
        }
    }

    /**
     * Get all simulated vehicles
     */
    public List<SimulatedVehicle> getVehicles() {
        return new ArrayList<>(vehicles);
    }

    /**
     * Get vehicle by ID
     */
    public SimulatedVehicle getVehicle(String vehicleId) {
        return vehicles.stream()
                .filter(v -> v.getVehicleId().equals(vehicleId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Manually start rental for a vehicle
     */
    public void startRental(String vehicleId, String rentalId) {
        SimulatedVehicle vehicle = getVehicle(vehicleId);
        if (vehicle != null) {
            vehicle.startRental(rentalId);
            log.info("Manually started rental for vehicle {}: rentalId={}", vehicleId, rentalId);
        }
    }

    /**
     * Manually end rental for a vehicle
     */
    public void endRental(String vehicleId) {
        SimulatedVehicle vehicle = getVehicle(vehicleId);
        if (vehicle != null) {
            vehicle.endRental();
            log.info("Manually ended rental for vehicle {}", vehicleId);
        }
    }
}
