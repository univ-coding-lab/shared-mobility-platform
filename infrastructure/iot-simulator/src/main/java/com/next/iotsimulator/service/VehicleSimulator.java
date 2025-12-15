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

    @Scheduled(fixedDelayString = "${simulator.update.interval:10000}")
    public void updateVehicles() {
        log.debug("Updating telemetry for {} vehicles", vehicles.size());

        for (SimulatedVehicle vehicle : vehicles) {
            try {

                if (!vehicle.isMoving() && random.nextDouble() < movingProbability) {
                    vehicle.startRental("rental-" + System.currentTimeMillis());
                    log.info("Vehicle {} started moving (rental started)", vehicle.getVehicleId());
                } else if (vehicle.isMoving() && random.nextDouble() < 0.1) {

                    vehicle.endRental();
                    log.info("Vehicle {} stopped (rental ended)", vehicle.getVehicleId());
                }

                if (vehicle.isMoving()) {
                    vehicle.move();
                }

                telemetryPublisher.publishTelemetry(vehicle);

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

    @Scheduled(fixedRate = 60000)
    public void chargeVehicles() {
        for (SimulatedVehicle vehicle : vehicles) {
            if (!vehicle.isMoving() && vehicle.getBatteryLevel() < 80) {

                vehicle.charge(10);
                log.debug("Charged vehicle {} to {}%", vehicle.getVehicleId(), vehicle.getBatteryLevel());
            }
        }
    }

    public List<SimulatedVehicle> getVehicles() {
        return new ArrayList<>(vehicles);
    }

    public SimulatedVehicle getVehicle(String vehicleId) {
        return vehicles.stream()
                .filter(v -> v.getVehicleId().equals(vehicleId))
                .findFirst()
                .orElse(null);
    }

    public void startRental(String vehicleId, String rentalId) {
        SimulatedVehicle vehicle = getVehicle(vehicleId);
        if (vehicle != null) {
            vehicle.startRental(rentalId);
            log.info("Manually started rental for vehicle {}: rentalId={}", vehicleId, rentalId);
        }
    }

    public void endRental(String vehicleId) {
        SimulatedVehicle vehicle = getVehicle(vehicleId);
        if (vehicle != null) {
            vehicle.endRental();
            log.info("Manually ended rental for vehicle {}", vehicleId);
        }
    }
}
